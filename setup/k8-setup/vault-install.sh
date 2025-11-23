#!/usr/bin/env bash

# Set default namespace (defaults to 'alpha-vault'), root database, root DB username, root DB password
CUR_NAMESPACE=${1-alpha-vault}

POSTGRES_DB_URL=${2-postgres.obp-local.svc.cluster.local:5432}
POSTGRES_DB_USER=${3-alpha}
POSTGRES_DB_PASS=${4-alpha}

MYSQL_DB_URL=${2-mysql.obp-local.svc.cluster.local:3306}
MYSQL_DB_USER=${3-root}
MYSQL_DB_PASS=${4-alpha}

# Function (Override PostgreSQL database defaults with MySQL defaults - unless they have been changed by command line arg)

# Function (Configure Vault)

function configureVault() {

  printf " - Creating policies ...\n\n"
  set -x

  kubectl exec -n $CUR_NAMESPACE alpha-vault-0 -- sh -c '
    vault secrets enable -path=internal kv-v2 && \
    vault secrets enable database && \
    vault auth enable kubernetes && \
    vault write auth/kubernetes/config \
      token_reviewer_jwt="$(cat /var/run/secrets/kubernetes.io/serviceaccount/token)" \
      kubernetes_host="https://$KUBERNETES_PORT_443_TCP_ADDR:443" \
      kubernetes_ca_cert=@/var/run/secrets/kubernetes.io/serviceaccount/ca.crt'

  { set +x; } 2>/dev/null
  printf "\nVault configured!\n\n"

}

# Function (configure database for auto secrets)

function configureDatabaseAutoSecrets() {
  database_vendor="$1"
  database_name="$2"

  printf "\nConfiguring $database_vendor auto secrets for database [ $database_name ]\n\n"

  # Postgres
  if [[ "$database_vendor" == "postgres" ]]; then
    set -x

    kubectl exec -n $CUR_NAMESPACE alpha-vault-0 -- vault write database/config/$database_name \
      plugin_name=postgresql-database-plugin \
      connection_url=postgresql://{{username}}:{{password}}@$POSTGRES_DB_URL/$database_name?sslmode=disable \
      allowed_roles=$database_name \
      username=$POSTGRES_DB_USER \
      password=$POSTGRES_DB_PASS
    kubectl exec -n $CUR_NAMESPACE alpha-vault-0 -- vault write database/roles/$database_name \
      db_name=$database_name \
      creation_statements=@/tmp/alpha-postgres-role-full.sql \
      default_ttl=1h \
      max_ttl=24h

    { set +x; } 2>/dev/null
  fi

  # MySQL
  if [[ "$database_vendor" == "mysql" ]]; then
    set -x

    kubectl exec -n $CUR_NAMESPACE alpha-vault-0 -- vault write database/config/$database_name \
      plugin_name=mysql-database-plugin \
      connection_url="{{username}}:{{password}}@tcp($MYSQL_DB_URL)/" \
      allowed_roles=$database_name \
      username=$MYSQL_DB_USER \
      password=$MYSQL_DB_PASS
    kubectl exec -n $CUR_NAMESPACE alpha-vault-0 -- vault write database/roles/$database_name \
      db_name=$database_name \
      creation_statements="CREATE USER '{{name}}'@'%' IDENTIFIED BY '{{password}}';GRANT ALL ON ${database_name}.* TO '{{name}}'@'%';" \
      default_ttl=1h \
      max_ttl=24h
    # grant all on temenos.* to 'alpha'@'%';
    { set +x; } 2>/dev/null
  fi

}

# Function (Create Policy / Role)

function createPolicyAndRole() {

  policy_file="$1"
  env="$2"
  namespaces="$3"
  printf " - Creating policies on env [ $env ] against namespaces [ $namespaces ] ...\n\n"
  set -x

  # 1. Upload (1) policy and (2) list of services to Vault pod
  kubectl cp _alpha-app-policy-${policy_file}.hcl $CUR_NAMESPACE/alpha-vault-0:/tmp/alpha-app-policy-${policy_file}.hcl
  kubectl cp _alpha-bound-services.txt $CUR_NAMESPACE/alpha-vault-0:/tmp/alpha-bound-services.txt

  # 2. Write uploaded Vault policy
  kubectl exec -n $CUR_NAMESPACE alpha-vault-0 -- vault policy write alpha-app-${env} /tmp/alpha-app-policy-${policy_file}.hcl

  # 3. Authorize Vault services / policies
  kubectl exec -n $CUR_NAMESPACE alpha-vault-0 -- vault write auth/kubernetes/role/alpha-app-${env} \
    bound_service_account_names=@/tmp/alpha-bound-services.txt \
    bound_service_account_namespaces=${namespaces} \
    policies=alpha-app-${env} \
    ttl=10m

  { set +x; } 2>/dev/null
  printf "\nVault policy created!\n\n"

}

printf "========================================\n"
printf "VAULT INSTALL SCRIPT\n"
printf "========================================\n"

# Check namespace exists and prompt user to create it if it doesn't exist

if ! $(kubectl get namespace $CUR_NAMESPACE >/dev/null); then
  echo
  read -r -p "Create namespace [ $CUR_NAMESPACE ]? [y/N] " response
  if [[ "$response" =~ ^([yY][eE][sS]|[yY])$ ]]; then
    kubectl create namespace $CUR_NAMESPACE
    echo
  else
    printf "\nBye!"
    exit 0
  fi
fi

# Loop until user types 'q' as an option

while :; do

  printf "\nCurrent pods in namespace [ $CUR_NAMESPACE ] ...\n\n"
  printf "$(kubectl get pods -n $CUR_NAMESPACE)\n\n"
  printf "Select an option to install (or 'q' to quit)\n"
  printf " (1) Install Vault in DEV mode (uses tar file)\n"
  printf " (2) Install Vault in HA mode with (PostgreSQL storage)\n"
  printf " (3) Install Vault in HA mode with (MySQL storage)\n\n"

  printf " (4) Install Auto Secrets for alpha databases (PostgreSQL)\n"
  printf " (5) Install Auto Secrets for alpha databases (MySQL)\n\n"

  printf " (6) Install policies - Local [ obp-local ]\n"
  printf " (7) Install policies - AWS [ obp-dev ]\n"
  printf " (8) Install policies - AWS [ obp-test ]\n\n"

  printf " (9) Run vault commands e.g. [ vault kv get internal/alpha/dev/card/api/key ] \n\n"
  printf " - "
  read -r choice

  # Quit

  if [[ "$choice" =~ ^([qQ])$ ]]; then
    printf "\nBye\n"
    exit 0
  fi

  # (1) Install Vault in DEV mode

  if [[ "$choice" == "1" ]]; then
    printf " - Installing ...\n\n"
    set -x

    # Extract help repo
    tar -xzvf vault-helm-install.tgz
    cd vault-helm-install

    helm template --name alpha-vault . --output-dir './yamls' --set "server.dev.enabled=true" --namespace $CUR_NAMESPACE

    # Apply template (run `kubectl apply -f yamls/vault/templates/<template>  -n alpha-vault --validate=false` for each template)
    for template in $(ls yamls/vault/templates); do
      kubectl apply -f yamls/vault/templates/${template} -n $CUR_NAMESPACE --validate=false
    done
    cd ..
    rm -dfr vault-helm-install

    { set +x; } 2>/dev/null

    # Wait until both Vault pods are all available (note the "True True")
    printf "\nWaiting for pods to become available: "
    while [[ $(kubectl get pods -n $CUR_NAMESPACE -o 'jsonpath={..status.conditions[?(@.type=="Ready")].status}') != "True True" ]]; do printf "." && sleep 1; done
    printf "\n\n$(kubectl get pods -n $CUR_NAMESPACE)\n\n"

    configureVault
  fi

  # (2) Install Vault in HA mode

  if [[ "$choice" == "2" ]] || [[ "$choice" == "3" ]]; then

    # Determine database (defaults to PostgreSQL)
    database_vendor="postgres"
    if [[ "$choice" == "3" ]]; then
      database_vendor="mysql"
      overrideDatabaseDefaultsMySql
    fi

    printf " - Installing using [ database_vendor ] storage ...\n\n"

    set -x

    # Create storage config secret
    kubectl create secret generic vault-storage-config --from-file=vault-storage-${database_vendor}-config.hcl -n $CUR_NAMESPACE

    # Git clone repo, checkout a stable release and create 'yamls' folder for generate templates
    git clone https://github.com/hashicorp/vault-helm.git
    cd vault-helm
    git checkout v0.4.0
    mkdir yamls

    # Replace 'v2' with 'v1' instead 'Chart.yaml' file (was causing issues with older version of Helm)
    sed -i'.backup' 's/v2/v1/' Chart.yaml

    # Generate templates based on the following properties

    helm template --name alpha-vault . --output-dir './yamls' \
      --namespace $CUR_NAMESPACE \
      --set="server.extraVolumes[0].type=secret" \
      --set="server.extraVolumes[0].name=vault-storage-config" \
      --set="server.extraVolumes[0].path=/vault/userconfig" \
      --set="server.ha.enabled=true" \
      --set="server.ha.replicas=1" \
      --set="server.extraArgs=-config=/vault/userconfig/vault-storage-config/vault-storage-postgres-config.hcl"

    # # Apply template (run `kubectl apply -f yamls/vault/templates/<template>  -n alpha-vault --validate=false` for each template)
    for template in $(ls yamls/vault/templates); do
      kubectl apply -f yamls/vault/templates/${template} -n $CUR_NAMESPACE --validate=false
    done
    cd ..
    rm -dfr vault-helm

    # # Sleep for a few seconds
    # printf "\nWaiting for pods to become available: "
    # # while [[ $(kubectl get pods -n $CUR_NAMESPACE -o 'jsonpath={..status.conditions[?(@.type=="Ready")].status}') != "True True" ]]; do printf "." && sleep 1; done

    sleep 15

    # # Initialise Vault
    # # !!! NOTE !!! - In production we should have [ -key-shares=5 -key-threshold=3 ]
    kubectl exec -n $CUR_NAMESPACE alpha-vault-0 -- vault operator init -key-shares=1 -key-threshold=1 | tee vault-keys.txt

    # Now unseal (use first key in the saved 'vault-keys.txt' file)
    kubectl exec -n $CUR_NAMESPACE alpha-vault-0 -- \
      vault operator unseal $(cat vault-keys.txt | grep 'Unseal Key 1' | cut -c 15-)

    # Finally, authorize vault (use Root Token in the saved 'vault-keys.txt' file)
    kubectl exec -n $CUR_NAMESPACE alpha-vault-0 -- \
      vault login $(cat vault-keys.txt | grep 'Root Token' | cut -c 21-)

    # Vault can now be configured in the usual manner
    configureVault

    { set +x; } 2>/dev/null

  fi

  # (4,5) Configure database auto-secrets against PostgreSQL / MySQL

  if [[ "$choice" == "4" ]] || [[ "$choice" == "5" ]]; then

    database_vendor="postgres"
    if [[ "$choice" == "5" ]]; then
      database_vendor="mysql"
    fi

    # Upload database rotation SQL file to Vault pod (only applicable for PostgreSQL)
    if [[ "$database_vendor" == "postgres" ]]; then
      kubectl cp _alpha-postgres-role-full.sql $CUR_NAMESPACE/alpha-vault-0:/tmp/alpha-postgres-role-full.sql
    fi

    # Configure auto secrets against all the Alpha databases.
    configureDatabaseAutoSecrets $database_vendor audit
    configureDatabaseAutoSecrets $database_vendor banks
    configureDatabaseAutoSecrets $database_vendor cards
    configureDatabaseAutoSecrets $database_vendor cases
    configureDatabaseAutoSecrets $database_vendor certificate
    configureDatabaseAutoSecrets $database_vendor customers
    configureDatabaseAutoSecrets $database_vendor flowable
    configureDatabaseAutoSecrets $database_vendor insights
    configureDatabaseAutoSecrets $database_vendor loans
    configureDatabaseAutoSecrets $database_vendor openbanking
    configureDatabaseAutoSecrets $database_vendor passkit
    configureDatabaseAutoSecrets $database_vendor payments
    configureDatabaseAutoSecrets $database_vendor temenos
  fi

  # (6,7,8) Create Vault policy / roles against different environments / policies

  if [[ "$choice" == "6" ]]; then
    createPolicyAndRole local dev obp-dev,obp-pre-dev,obp-local,obp-local-infra,kafka
  fi
  if [[ "$choice" == "7" ]]; then
    createPolicyAndRole dev dev obp-dev,obp-pre-dev
  fi
  if [[ "$choice" == "8" ]]; then
    createPolicyAndRole test test obp-test
  fi

  # (9) Run vault commands

  if [[ "$choice" == "9" ]]; then
    kubectl exec -it alpha-vault-0 -n $CUR_NAMESPACE -- sh
  fi

  # Keep looping
done

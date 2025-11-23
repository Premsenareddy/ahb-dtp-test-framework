#!/usr/bin/env bash

# Set default namespace (defaults to 'alpha-vault'), root database, root DB username, root DB password
CUR_NAMESPACE=${1-alpha-vault}

POSTGRES_DB_URL=${2-postgres.obp-local.svc.cluster.local:5432}
POSTGRES_DB_USER=${3-alpha}
POSTGRES_DB_PASS=${4-alpha}


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



  # (1) Install Vault in DEV mode

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

echo "Waiting for vault to start"

  sleep 10
  createPolicyAndRole local dev obp-local,obp-local-infra
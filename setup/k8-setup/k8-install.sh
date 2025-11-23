#!/usr/bin/env bash

# Set default namespace (defaults to 'obp-local')
CUR_NAMESPACE=${1-obp-local-infra}

printf "========================================\n"
printf "K8 INSTALL SCRIPT\n"
printf "========================================\n"


#Cleanup from previous runs


# Check namespace exists and prompt user to create it if it doesn't exist

if $(kubectl get namespace $CUR_NAMESPACE >/dev/null); then
  printf "Current pods in namespace [ $CUR_NAMESPACE ] ...\n"
  printf "\n$(kubectl get pods -n $CUR_NAMESPACE)\n"
  printf "========================================\n\n"
else
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
  printf "Select an option to install (OR q=quit, p=pods)\n"
  printf " (1) Install PostgreSQL \n"
  printf " (2) Install Redis \n"
  printf " (3) Install Kafka \n"
  printf " (4) Install MySQL \n\n"
  printf " (5) PostgreSQL - run ad hoc database commands (psql) e.g. [ create database payment; ] \n"
  printf " (6) PostgreSQL - create alpha databases (postgresql)\n"
  printf " (7) PostgreSQL - create vault database + schema \n\n"
  printf " (8) MySQL - run ad hoc database commands (mysql) e.g. [ create database payment; ] \n"
  printf " (9) MySQL - create alpha databases\n"
  printf " - "
  read -r choice

  # Quit

  if [[ "$choice" =~ ^([qQ])$ ]]; then
    printf "\nBye\n"
    exit 0
  fi

  # Watch k8 pods

  if [[ "$choice" =~ ^([pP])$ ]]; then
    watch kubectl get pods -n $CUR_NAMESPACE
  fi

  # (1) PostgreSQL

  if [[ "$choice" == "1" ]]; then
    printf " - Installing ...\n\n"
    set -x

    kubectl apply -f k8-postgresql.yaml -n $CUR_NAMESPACE

    { set +x; } 2>/dev/null
    printf "\nPostgreSQL installed!\n\n"
  fi

  # (2) Redis
  if [[ "$choice" == "2" ]]; then
    printf " - Installing ...\n\n"
    set -x

    helm install --name local --namespace $CUR_NAMESPACE --set usePassword=false --set cluster.enabled=false stable/redis

    { set +x; } 2>/dev/null
    printf "\nRedis Installed!\n\n"
  fi

  # (3) Kafka
  if [[ "$choice" == "3" ]]; then
    printf " - Installing ...\n\n"
    set -x
    helm repo add incubator https://kubernetes-charts-incubator.storage.googleapis.com
    helm install --name kafka --namespace kafka --set replicas=1 --set partitions=3 --set zookeeper.replicaCount=1 incubator/kafka

    { set +x; } 2>/dev/null
    printf "\nKafka Installed!\n\n"
  fi

  # (4) MySQL

  if [[ "$choice" == "4" ]]; then
    printf " - Installing ...\n\n"
    set -x

    helm install --name mysql --namespace $CUR_NAMESPACE --set mysqlRootPassword=alpha stable/mysql

    { set +x; } 2>/dev/null
    printf "\nMySQL Installed!\n\n"
  fi

  # (5) Run PostgreSQL database commands

  if [[ "$choice" == "5" ]]; then

    printf "====================================================\n"
    printf "Useful commands:\n"
    printf "====================================================\n"
    printf "  \q        Quit\n"
    printf "  \l        List databases\n"
    printf "  \c <db>   Connect to database e.g. \c cards\n"
    printf "  \dt       Display tables in database\n"
    printf "  \du       Display users in database\n"
    printf "====================================================\n"

    POSTGRES_POD="$(kubectl get pods -l app=postgres -ojsonpath='{range .items[*]}{.metadata.name}{"\n"}{end}' -n $CUR_NAMESPACE)"

    kubectl exec -it $POSTGRES_POD -n $CUR_NAMESPACE -- psql -U alpha postgres

  fi

  # (6) Create Postgres alpha databases

  if [[ "$choice" == "6" ]]; then

    POSTGRES_POD="$(kubectl get pods -l app=postgres -ojsonpath='{range .items[*]}{.metadata.name}{"\n"}{end}' -n $CUR_NAMESPACE)"

    # 1. Copy policy to PosgreSQL pod
    kubectl cp _alpha-postgres-create-databases.sql $CUR_NAMESPACE/$POSTGRES_POD:/tmp/alpha-postgres-create-databases.sql

    # 2. Execute script to create all databases
    kubectl exec $POSTGRES_POD -n $CUR_NAMESPACE -- psql -U alpha postgres -f /tmp/alpha-postgres-create-databases.sql

  fi

  # (7) Create Vault schema

  if [[ "$choice" == "7" ]]; then

    POSTGRES_POD="$(kubectl get pods -l app=postgres -ojsonpath='{range .items[*]}{.metadata.name}{"\n"}{end}' -n $CUR_NAMESPACE)"

    # 1. Copy policy to PosgreSQL pod
    kubectl cp _vault-postgresql-create-storage-tables.sql $CUR_NAMESPACE/$POSTGRES_POD:/tmp/vault-postgresql-create-storage-tables.sql

    # 2. Create vault database
    kubectl exec $POSTGRES_POD -n $CUR_NAMESPACE -- psql -U alpha postgres -c 'create database vault'

    # 3. Execute Vault script
    kubectl exec $POSTGRES_POD -n $CUR_NAMESPACE -- psql -U alpha vault -f /tmp/vault-postgresql-create-storage-tables.sql

  fi

  # (8) Run MySQL database commands

  if [[ "$choice" == "8" ]]; then

    printf "===================================================================\n"
    printf "Useful commands:\n"
    printf "===================================================================\n"
    printf "  quit                           Quit\n"
    printf "  show databases;                List databases\n"
    printf "  use <db>;                      Connect to database e.g. use cards;\n"
    printf "  show tables;                   Display tables in database\n"
    printf "  select user from mysql.user;   List users\n"
    printf "  show grants for '<user>';      Show grants for a specific user\n"
    printf "===================================================================\n"

    MYSQL_POD="$(kubectl get pods -l app=mysql -ojsonpath='{range .items[*]}{.metadata.name}{"\n"}{end}' -n $CUR_NAMESPACE)"

    kubectl exec -it $MYSQL_POD -n $CUR_NAMESPACE -- sh -c 'MYSQL_PWD=alpha mysql -u root'

  fi

  # (9) Create MySQL alpha databases

  if [[ "$choice" == "9" ]]; then

    MYSQL_POD="$(kubectl get pods -l app=mysql -ojsonpath='{range .items[*]}{.metadata.name}{"\n"}{end}' -n $CUR_NAMESPACE)"

    printf "\nCreating database tables, 'alpha' user created and applying grants ...\n\n"

    set -x

    # 1. Copy policy to PosgreSQL pod and execute
    kubectl cp _alpha-mysql-create-databases.sql $CUR_NAMESPACE/$MYSQL_POD:/tmp/alpha-mysql-create-databases.sql
    kubectl exec $MYSQL_POD -n $CUR_NAMESPACE -- sh -c 'MYSQL_PWD=alpha mysql -u root < /tmp/alpha-mysql-create-databases.sql'

    { set +x; } 2>/dev/null
    printf "\nUpdated successfully!\n\n"

    #kubectl exec $MYSQL_POD -n $CUR_NAMESPACE -- sh -c 'MYSQL_PWD=alpha mysql -u root -e "show databases"'
  fi

  # Keep looping
done

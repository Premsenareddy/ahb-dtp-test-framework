#!/usr/bin/env bash

# Set default namespace (defaults to 'obp-local')
CUR_NAMESPACE=${1-obp-local-infra}

printf "========================================\n"
printf "K8 POSTGRES INSTALL SCRIPT\n"
printf "========================================\n"


  kubectl create namespace $CUR_NAMESPACE

  printf " - Installing ...\n\n"
  set -x

  kubectl create -f k8-postgresql.yaml -n $CUR_NAMESPACE

  { set +x; } 2>/dev/null
  printf "\nPostgreSQL installed!\n\n"


# (6) Create Postgres alpha databases

echo "Waiting for database to start"
sleep 10


POSTGRES_POD="$(kubectl get pods -l app=postgres -ojsonpath='{range .items[*]}{.metadata.name}{"\n"}{end}' -n $CUR_NAMESPACE)"

# 1. Copy policy to PosgreSQL pod
kubectl cp _alpha-postgres-create-databases.sql $CUR_NAMESPACE/$POSTGRES_POD:/tmp/alpha-postgres-create-databases.sql

# 2. Execute script to create all databases
kubectl exec $POSTGRES_POD -n $CUR_NAMESPACE -- psql -U alpha postgres -f /tmp/alpha-postgres-create-databases.sql


echo "Done"
#!/usr/bin/env bash

# Set default namespace (defaults to 'alpha-vault')
CUR_NAMESPACE=${1-alpha-vault}

printf "========================================\n"
printf "VAULT SET SECRETS\n"
printf "========================================\n"

# Check namespace exists and prompt user to create if it doesn't

if $(kubectl get namespace $CUR_NAMESPACE >/dev/null); then
  printf " - Setting Secrets ...\n\n"
else
  echo
  printf "\nNamespace [ $CUR_NAMESPACE ] does not exist!!! (run 'vault-install.sh' script to set up Vault)\n\n"
  exit 0
fi

# Show commands
set -x

kubectl cp _alpha-secrets.sh $CUR_NAMESPACE/alpha-vault-0:/tmp/alpha-secrets.sh
kubectl exec alpha-vault-0 -n $CUR_NAMESPACE -- sh /tmp/alpha-secrets.sh

# Don't show commands
{ set +x; } 2>/dev/null

printf "\nSecrets Set!\n\n"

#!/usr/bin/env bash

# Set default namespace (defaults to 'alpha-vault')
CUR_NAMESPACE=${1-alpha-vault}

printf "========================================\n"
printf "VAULT UN-INSTALL SCRIPT\n"
printf "========================================\n"

# Loop until user types 'q' as an option
while :; do

  printf "\nCurrent pods in namespace [ $CUR_NAMESPACE ] ...\n\n"
  printf "$(kubectl get pods -n $CUR_NAMESPACE)\n\n"
  printf "\nSelect an option to un-install (or 'q' to quit)\n"
  printf " (1) Vault (in-progress)\n"
  printf " (2) Delete entire namespace [ $CUR_NAMESPACE ]\n"
  printf " - "
  read -r choice

  # Quit

  if [[ "$choice" =~ ^([qQ])$ ]]; then
    printf "\nBye\n"
    exit 0
  fi

  # (1) Vault

  if [[ "$choice" == "1" ]]; then
    read -r -p "Are you sure you want to un-install Vault? [y/N] " response
    if [[ "$response" =~ ^([yY][eE][sS]|[yY])$ ]]; then
      printf " - Uninstalling ...\n\n"
      set -x

      kubectl delete service vault -n $CUR_NAMESPACE
      kubectl delete deployment vault-agent-injector -n $CUR_NAMESPACE

      { set +x; } 2>/dev/null
      printf "\nPostgreSQL Uninstalled!\n\n"
    fi
  fi

  # (2) Namespace

  if [[ "$choice" == "2" ]]; then
    read -r -p "Are you sure you want to delete entire namespace [ $CUR_NAMESPACE ]? [y/N] " response
    if [[ "$response" =~ ^([yY][eE][sS]|[yY])$ ]]; then
      printf " - Deleting ...\n\n"
      set -x

      kubectl delete ns $CUR_NAMESPACE

      { set +x; } 2>/dev/null
      printf "\nNamespace deleted!\n\n"

      # No point in continuing as this is the nuclear option
      printf "\nBye\n"
      exit 0
    fi
  fi

  # Keep looping
done

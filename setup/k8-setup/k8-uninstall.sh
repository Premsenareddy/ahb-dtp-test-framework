#!/usr/bin/env bash

# Set default namespace (defaults to 'obp-local')
CUR_NAMESPACE=${1-obp-local}

printf "========================================\n"
printf "K8 UN-INSTALL SCRIPT\n"
printf "========================================\n"

# Loop until user types 'q' as an option
while :; do

  printf "\nCurrent pods in namespace [ $CUR_NAMESPACE ] ...\n\n"
  printf "$(kubectl get pods -n $CUR_NAMESPACE)\n\n"
  printf "\nSelect an option to un-install (or 'q' to quit)\n"
  printf " (1) PostgreSQL\n"
  printf " (2) Redis\n"
  printf " (3) Kafka\n"
  printf " (4) MySQL\n\n"
  printf " (5) Delete entire namespace [ $CUR_NAMESPACE ]\n\n"
  printf " - "
  read -r choice

  # Quit

  if [[ "$choice" =~ ^([qQ])$ ]]; then
    printf "\nCurrent pods in namespace [ $CUR_NAMESPACE ] ...\n"
    printf "\n$(kubectl get pods -n $CUR_NAMESPACE)\n"
    printf "\nBye\n"
    exit 0
  fi

  # (1) PostgreSQL

  if [[ "$choice" == "1" ]]; then
    read -r -p "Are you sure you want to un-install PostgreSQL? [y/N] " response
    if [[ "$response" =~ ^([yY][eE][sS]|[yY])$ ]]; then
      printf " - Uninstalling ...\n\n"
      set -x

      kubectl delete service postgres -n $CUR_NAMESPACE
      kubectl delete deployments.extensions "postgres" -n $CUR_NAMESPACE
      kubectl delete configmaps "postgres-config" -n $CUR_NAMESPACE

      { set +x; } 2>/dev/null
      printf "\nPostgreSQL Uninstalled!\n\n"
    fi
  fi

  # (2) Redis

  if [[ "$choice" == "2" ]]; then
    read -r -p "Are you sure you want to un-install Redis? [y/N] " response
    if [[ "$response" =~ ^([yY][eE][sS]|[yY])$ ]]; then
      printf " - Uninstalling ...\n\n"
      set -x

      helm del --purge local

      { set +x; } 2>/dev/null
      printf "\nRedis Uninstalled!\n\n"
    fi
  fi

  # (3) Kafka

  if [[ "$choice" == "3" ]]; then
    read -r -p "Are you sure you want to un-install Kafka? [y/N] " response
    if [[ "$response" =~ ^([yY][eE][sS]|[yY])$ ]]; then
      printf " - Uninstalling ...\n\n"
      set -x

      helm del --purge kafka

      { set +x; } 2>/dev/null
      printf "\nKafka Uninstalled!\n\n"
    fi
  fi

  # (4) MySQL

  if [[ "$choice" == "4" ]]; then
    read -r -p "Are you sure you want to un-install MySQL? [y/N] " response
    if [[ "$response" =~ ^([yY][eE][sS]|[yY])$ ]]; then
      printf " - Uninstalling ...\n\n"
      set -x

      helm del --purge mysql

      { set +x; } 2>/dev/null
      printf "\nRedis Uninstalled!\n\n"
    fi
  fi

  # (5) Namespace

  if [[ "$choice" == "5" ]]; then
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

#!/usr/bin/env bash

function exportOrDefault {
  VAR_NAME=$1
  if [[ "$#" -ne 3 ]]; then
    echo "${VAR_NAME} is empty. Setting..."
    eval "${VAR_NAME}"=$2
    # shellcheck disable=SC2163
    export ${VAR_NAME}
  else
    echo "${VAR_NAME} has already been set"
  fi
}

function retrieveKeyVaultSecret() {
  SECRET=$1

  az keyvault secret show \
    --name ${SECRET} \
    --vault-name ${AZ_VAULT_NAME} \
    --output none 2>/dev/null

  # shellcheck disable=SC2181
  if [[ $? -eq 0 ]]; then
    SECRET_VALUE=$(az keyvault secret show --name ${SECRET} \
      --vault-name ${AZ_VAULT_NAME} 2>/dev/null | jq -r '.value')
    echo "${SECRET_VALUE}"
  else
    exit 151
  fi
}

function retrieveTableStoreConfig() {
  CONFIG_NAME=$1
  SECRET=$2

  az storage entity show -o none \
    --account-name ${STORAGE_ACCOUNT_NAME} \
    --table-name ${TABLE_NAME} \
    --partition-key ${PARTITION_KEY} \
    --row-key ${CONFIG_NAME} 2>/dev/null

  # shellcheck disable=SC2181
  if [[ $? -eq 0 ]]; then
    CONFIG_VALUE=$(az storage entity show \
      --account-name ${STORAGE_ACCOUNT_NAME} \
      --table-name ${TABLE_NAME} \
      --partition-key ${PARTITION_KEY} \
      --row-key ${CONFIG_NAME} 2>/dev/null | jq -r '.Content')

    if [[ -n ${SECRET} ]] && [[ ${SECRET} == "false" ]]; then
      echo "${CONFIG_VALUE}"
    else
      SECRET_VALUE="$(retrieveKeyVaultSecret "${CONFIG_VALUE}")"
      echo "${SECRET_VALUE}"
    fi

  else
    exit 151
  fi
}

case ${TABLE_NAME} in
  sandbox)
    exportOrDefault "NON_PROD_EXPERIENCE_DEVELOPER_OBJECT_ID" $NON_PROD_EXPERIENCE_DEVELOPER_OBJECT_ID "$(retrieveTableStoreConfig "NON_PROD_EXPERIENCE_DEVELOPER_OBJECT_ID")"
    exportOrDefault "NON_PROD_SERVICE_DEVELOPER_OBJECT_ID" $NON_PROD_SERVICE_DEVELOPER_OBJECT_ID "$(retrieveTableStoreConfig "NON_PROD_SERVICE_DEVELOPER_OBJECT_ID")"
    exportOrDefault "NON_PROD_PLATFORM_DEVELOPER_OBJECT_ID" $NON_PROD_PLATFORM_DEVELOPER_OBJECT_ID "$(retrieveTableStoreConfig "NON_PROD_PLATFORM_DEVELOPER_OBJECT_ID")"
    exportOrDefault "NON_PROD_DEVOPS_OBJECT_ID" $NON_PROD_DEVOPS_OBJECT_ID "$(retrieveTableStoreConfig "NON_PROD_DEVOPS_OBJECT_ID")"
    ;;
  dev)
    exportOrDefault "NON_PROD_EXPERIENCE_DEVELOPER_OBJECT_ID" $NON_PROD_EXPERIENCE_DEVELOPER_OBJECT_ID "$(retrieveTableStoreConfig "NON_PROD_EXPERIENCE_DEVELOPER_OBJECT_ID")"
    exportOrDefault "NON_PROD_SERVICE_DEVELOPER_OBJECT_ID" $NON_PROD_SERVICE_DEVELOPER_OBJECT_ID "$(retrieveTableStoreConfig "NON_PROD_SERVICE_DEVELOPER_OBJECT_ID")"
    exportOrDefault "NON_PROD_PLATFORM_DEVELOPER_OBJECT_ID" $NON_PROD_PLATFORM_DEVELOPER_OBJECT_ID "$(retrieveTableStoreConfig "NON_PROD_PLATFORM_DEVELOPER_OBJECT_ID")"
    exportOrDefault "NON_PROD_DEVOPS_OBJECT_ID" $NON_PROD_DEVOPS_OBJECT_ID "$(retrieveTableStoreConfig "NON_PROD_DEVOPS_OBJECT_ID")"
    ;;
  nft)
    exportOrDefault "NON_PROD_EXPERIENCE_DEVELOPER_OBJECT_ID" $NON_PROD_EXPERIENCE_DEVELOPER_OBJECT_ID "$(retrieveTableStoreConfig "NON_PROD_EXPERIENCE_DEVELOPER_OBJECT_ID")"
    exportOrDefault "NON_PROD_SERVICE_DEVELOPER_OBJECT_ID" $NON_PROD_SERVICE_DEVELOPER_OBJECT_ID "$(retrieveTableStoreConfig "NON_PROD_SERVICE_DEVELOPER_OBJECT_ID")"
    exportOrDefault "NON_PROD_PLATFORM_DEVELOPER_OBJECT_ID" $NON_PROD_PLATFORM_DEVELOPER_OBJECT_ID "$(retrieveTableStoreConfig "NON_PROD_PLATFORM_DEVELOPER_OBJECT_ID")"
    exportOrDefault "NON_PROD_DEVOPS_OBJECT_ID" $NON_PROD_DEVOPS_OBJECT_ID "$(retrieveTableStoreConfig "NON_PROD_DEVOPS_OBJECT_ID")"
    ;;
  stg)
    exportOrDefault "STAGING_EXPERIENCE_DEVELOPER_OBJECT_ID" $NON_PROD_EXPERIENCE_DEVELOPER_OBJECT_ID "$(retrieveTableStoreConfig "STAGING_EXPERIENCE_DEVELOPER_OBJECT_ID")"
    exportOrDefault "STAGING_PLATFORM_DEVELOPER_OBJECT_ID" $NON_PROD_PLATFORM_DEVELOPER_OBJECT_ID "$(retrieveTableStoreConfig "STAGING_PLATFORM_DEVELOPER_OBJECT_ID")"
    exportOrDefault "STAGING_SERVICE_DEVELOPER_OBJECT_ID" $NON_PROD_SERVICE_DEVELOPER_OBJECT_ID "$(retrieveTableStoreConfig "STAGING_SERVICE_DEVELOPER_OBJECT_ID")"
    ;;
  sharedservices)
    exportOrDefault "SS_EXPERIENCE_DEVELOPER_OBJECT_ID" $SS_EXPERIENCE_DEVELOPER_OBJECT_ID "$(retrieveTableStoreConfig "SS_EXPERIENCE_DEVELOPER_OBJECT_ID")"
    exportOrDefault "SS_SERVICE_DEVELOPER_OBJECT_ID" $SS_SERVICE_DEVELOPER_OBJECT_ID "$(retrieveTableStoreConfig "SS_SERVICE_DEVELOPER_OBJECT_ID")"
    exportOrDefault "SS_PLATFORM_DEVELOPER_OBJECT_ID" $SS_PLATFORM_DEVELOPER_OBJECT_ID "$(retrieveTableStoreConfig "SS_PLATFORM_DEVELOPER_OBJECT_ID")"
    ;;
esac

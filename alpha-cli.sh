#!/usr/bin/env bash
command='./gradlew clean run'

if [[ $# -eq 0 ]]; then

  echo "Use: ./alpha-cli.sh <option> "
  echo " eg. ./alpha-cli.sh 1"
  echo
  echo "Insert the option as per below:"
  echo '1 :  Run locally to local instances'
  echo '2 :  Run locally to instances on the dev environment'
  echo '3 :  Run locally default'
  echo '4 :  Run locally on Kube via scaffold'
  echo '5 :  Refresh Kube config'
  read -p '>> ' option
else
  option=$1
fi

echo 'Option chosen > ' $option

function refreshKubeConfig() {

  export SERVICE_NAME=ahb-dtp-test-framework
  echo "Service -> ${SERVICE_NAME}"

  export ENV=dev
  export APP_IMAGE_NAME=${SERVICE_NAME}
  export IMG_TAG_TO_DEPLOY=alpha-local/${SERVICE_NAME}

  envsubst <kube/k8s-resources.yaml >k8s-resources.generated.yaml

  kubectl delete configMap ${APP_IMAGE_NAME} -n obp-local

  kubectl apply -f kube/configMap.yaml -n obp-local
  kubectl patch configMap alpha-common-config --patch "$(cat kube/local/configMap-local-patch.yaml)" -n obp-local

}

case $option in
1)
  profile="local,scratch"
  echo 'Run locally using local instances... ' $profile
  MICRONAUT_ENVIRONMENTS=$profile $command
  ;;
2)
  profile="local,dev,scratch"
  echo 'Run locally to instances on the dev environment'
  MICRONAUT_ENVIRONMENTS=$profile $command
  ;;
3)
  echo 'Run locally default'
  $command
  ;;
4)
  echo 'Run locally in kube via scaffold'
  refreshKubeConfig
  skaffold dev --port-forward
  ;;
5)
  echo 'Refreshing kube config'
  #TOOD:: File watch for changes
  refreshKubeConfig
  ;;
*)
  echo 'Invalid option ' $option
  ;;

esac

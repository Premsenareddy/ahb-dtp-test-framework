#!/usr/bin/env bash

##To build env config eg for cit:
## ./alpha-k8-cli.sh config-generate 8a7479b obp-cit ahbaenssdbpnpdacrtmp.azurecr.io cit

CONFIG_FILE=~/.alpha-local-deployment.sh
export SERVICE_NAME=${PWD##*/}

CURRENT_CLUSTER=$(kubectl config current-context)

if [ $# -gt 2 ]
  then
    echo "Using command line arguments"
    export BUILD_VARIANT=$1
    export VERSION=$2
    export NAMESPACE=$3
    export REGISTRY=$4
    export ENV_DEPLOY=$5
  elif [ -f "$CONFIG_FILE" ]; then
    echo "Using configuration file for configuration located at ~/.alpha-local-deployment.sh"
    source ~/.alpha-local-deployment.sh
  else
    echo "No arguments supplied: usage alpha-k8-cli.sh <operation build|config> <version> <namespace> <registry(optional)>"
    echo "Note if no registry is specified it will deploy to obp-local namespace"
    echo "if a remote registry is deployed it will deploy to obp-dev"
    echo ""
    echo "if no parameters are specified it will try and load from ~/.alpha-local-deployment.sh"
    echo "example of  ~/.alpha-local-deployment.sh is"
    echo "export NAMESPACE=obp-dev\nexport VERSION=1.0\nexport BUILD_VARIANT=build\nexport REGISTRY=kirk:5000"
    echo ""
    exit 0
fi

if [ -z $REGISTRY ]
  then
    export IMG_TAG_TO_DEPLOY=$SERVICE_NAME:$VERSION
  else
    export IMG_TAG_TO_DEPLOY=$REGISTRY/alpha-local/$SERVICE_NAME:$VERSION
fi

echo ""
echo "Running Alpha k8s cli with:\nBUILD_VARIANT=$BUILD_VARIANT\nVERSION=$VERSION\nREGISTRY=$REGISTRY\nNAMESPACE=$NAMESPACE"
echo ""

echo "Getting service name from current directory name"
echo "Service -> ${SERVICE_NAME}"

function deployConfig() {

    if [ "$CURRENT_CLUSTER" == "default" ] || [ "$CURRENT_CLUSTER" == "docker-desktop" ]
      then
        echo "Logged into local cluster proceeding"
      else
        echo "It looks like the current K8S context is against a remote cloud.  This is not supported by this script"
        exit 1
    fi

    echo Deploying $VERSION ...
    ## Note do not use latest
    export ENV=dev
    export APP_IMAGE_NAME=$SERVICE_NAME

    if [ -z $REGISTRY ]
      then
        echo "Deploying against local docker registry"
        envsubst < kube/k8s-resources.yaml > k8s-resources.generated.yaml
        cat k8s-resources.generated.yaml |  sed 's/Always/Never/g' > k8s-resources.generated.local.yaml
      else
        echo "Deploying against remote docker registry"
        envsubst < kube/k8s-resources.yaml > k8s-resources.generated.local.yaml
    fi

    kubectl delete deployment ${SERVICE_NAME}-v1 -n $NAMESPACE
    kubectl delete configMap ${SERVICE_NAME} -n $NAMESPACE

    kubectl apply -f kube/configMap.yaml -n $NAMESPACE
    kubectl -n $NAMESPACE apply -f k8s-resources.generated.local.yaml
}

function deployConfigToEnv() {

    echo Deploying $VERSION ...
    ## Note do not use latest
    export ENV=$ENV_DEPLOY
    export IMG_TAG_TO_DEPLOY=$REGISTRY/$SERVICE_NAME:$VERSION
    export APP_IMAGE_NAME=$SERVICE_NAME


    echo "Deploying against remote docker registry"
    envsubst < kube/k8s-resources.yaml > k8s-resources.generated.${ENV}.yaml

    echo "COMMANDS:: --- Run at your peril.. ----"
    echo docker build . -t $REGISTRY/$SERVICE_NAME:$VERSION
    echo docker push $REGISTRY/$SERVICE_NAME:$VERSION
    echo kubectl -n $NAMESPACE apply -f k8s-resources.generated.${ENV}.yaml
}


if [ "$BUILD_VARIANT" == "build" ]
then
    echo Building $VERSION ...
    ./gradlew clean build -x test
    docker build -t $IMG_TAG_TO_DEPLOY  .
    if [ ! -z $REGISTRY ]
    then
      echo "Pushing image to remote registry"
      docker push $IMG_TAG_TO_DEPLOY
    fi
    deployConfig

fi

if [ "$BUILD_VARIANT" == "config" ]
then
    deployConfig
fi

if [ "$BUILD_VARIANT" == "config-generate" ]
then
    deployConfigToEnv
fi

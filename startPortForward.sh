#!/usr/bin/env bash

if [[ $# -ne 1 ]]; then
  echo "Use: ./startPortForward.sh <namespace> "
  echo " eg. ./startPortForward.sh obp-dev "
  exit -1
fi


./stopPortForward.sh

export NAMESPACE=$1
echo "Port forwarding to ${NAMESPACE}"

kubectl port-forward svc/ahb-dtp-test-framework 8180:8080 -n ${NAMESPACE}
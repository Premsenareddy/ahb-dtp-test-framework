#!/usr/bin/env bash

if [[ $# -ne 2 ]]; then
  echo "Use: ./startPortForward.sh <namespace> <port> "
  echo " eg. ./startPortForward.sh obp-dev 8180 "
  exit -1
fi


# ./stopPortForward.sh

export NAMESPACE=$1
export PORT=$2
echo "Port forwarding to ${NAMESPACE}"

kubectl port-forward svc/ahb-dtp-test-framework ${PORT}:8080 -n ${NAMESPACE}
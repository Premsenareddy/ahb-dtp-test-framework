#!/usr/bin/env bash

set -e

NAMESPACE=elastic-search-local

#kubectl create namespace elastic-search-local

kubectl apply -f elasticsearch-deployment.yaml -n $NAMESPACE
kubectl apply -f kibana-deployment.yaml -n $NAMESPACE
kubectl apply -f logstash-deployment.yaml -n $NAMESPACE
kubectl apply -f filebeat-deployment.yaml -n $NAMESPACE

#!/usr/bin/env bash

echo "Port forwarding to ${K8S_NAMESPACE}"
kubectl port-forward svc/ahb-dtp-test-framework 8180:8080 -n ${K8S_NAMESPACE} > /dev/null 2>&1 &

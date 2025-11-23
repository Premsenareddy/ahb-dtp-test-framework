#!/bin/bash

echo -e "\nDeleting Istio resources"
kubectl delete -f istio -n ${K8S_NAMESPACE} && echo

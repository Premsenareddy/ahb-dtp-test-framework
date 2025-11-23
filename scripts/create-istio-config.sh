#!/bin/bash

echo -e "\nCreating Istio resources"
kubectl delete -f istio/${K8S_NAMESPACE} -n ${K8S_NAMESPACE} --ignore-not-found && echo
kubectl create -f istio/${K8S_NAMESPACE} -n ${K8S_NAMESPACE} && echo

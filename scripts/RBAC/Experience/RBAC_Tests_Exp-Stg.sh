#!/bin/bash

source ./scripts/default-env-vars.sh
source ./scripts/testing-utils.sh

groupID=${NON_PROD_EXPERIENCE_DEVELOPER_OBJECT_ID}

function kube-test-expected-true() {
  verb="$1"
  resource="$2"
  nonResourceUrl="$3"

  if [[ $(kubectl auth can-i "${verb}" "${resource}" --subresource="${nonResourceUrl}" -n "${namespace}" --as ${userGroup} --as-group ${groupID}) == "yes" ]]; then
    printf "${GREEN}${verb} ${resource} ${nonResourceUrl} = Test Passed Successfully${NC}\n"
    let "totalPassed++"
    printf "Passed: ${totalPassed} / Failed: ${totalFailed}"
    echo
    echo
  else
    printf "${RED}${verb} ${resource} ${nonResourceUrl} = Test Failed${NC}\n"
    let "totalFailed++"
    printf "Passed: ${totalPassed} / Failed: ${totalFailed}"
    log="${RED}${verb} ${resource} ${nonResourceUrl} = Test Failed${NC}\n"
    test-logs "${log}"
    echo
    echo
  fi

}

function kube-test-expected-false() {
  verb="$1"
  resource="$2"
  nonResourceUrl="$3"

  if [[ $(kubectl auth can-i "${verb}" "${resource}" --subresource="${nonResourceUrl}" -n "${namespace}" --as ${userGroup} --as-group ${groupID}) == "yes" ]]; then
    printf "${RED}${verb} ${resource} ${nonResourceUrl} = Test Passed${NC}\n"
    let "totalFailed++"
    printf "Passed: ${totalPassed} / Failed: ${totalFailed}"
    log="${RED}${verb} ${resource} ${nonResourceUrl} = Test Failed${NC}\n"
    test-logs "${log}"
    echo
    echo
  else
    printf "${GREEN}${verb} ${resource} ${nonResourceUrl} = Test Failed Successfully${NC}\n"
    let "totalPassed++"
    printf "Passed: ${totalPassed} / Failed: ${totalFailed}"
    echo
    echo
  fi

}

set-namespace experience-stg
set-user-group AHB-NonProd-AKS-Experience-Developer

heading "pods exec tests"

test-task "An Experience Developer should only be able to create an exec into pods"
echo

kube-test-expected-false get pods exec
kube-test-expected-false list pods exec
kube-test-expected-false watch pods exec
kube-test-expected-true create pods exec
kube-test-expected-false update pods exec
kube-test-expected-false patch pods exec
kube-test-expected-false delete pods exec

heading "pods portforward tests"

test-task "An Experience Developer should only be able to create portforwards into pods"
echo

kube-test-expected-false get pods portforward
kube-test-expected-false list pods portforward
kube-test-expected-false watch pods portforward
kube-test-expected-true create pods portforward
kube-test-expected-false update pods portforward
kube-test-expected-false patch pods portforward
kube-test-expected-false delete pods portforward

heading "services portforward tests"

test-task "An Experience Developer should only be able to create portforwards into services"
echo

kube-test-expected-false get service portforward
kube-test-expected-false get service portforward
kube-test-expected-false list service portforward
kube-test-expected-false watch service portforward
kube-test-expected-true create service portforward
kube-test-expected-false update service portforward
kube-test-expected-false patch service portforward
kube-test-expected-false delete service portforward

heading "selfsubjectaccessreviews tests"

test-task "An Experience Developer should only be able to create selfsubjectaccessreviews"
echo

kube-test-expected-false get selfsubjectaccessreviews.authorization.k8s.io
kube-test-expected-false list selfsubjectaccessreviews.authorization.k8s.io
kube-test-expected-false watch selfsubjectaccessreviews.authorization.k8s.io
kube-test-expected-true create selfsubjectaccessreviews.authorization.k8s.io
kube-test-expected-false update selfsubjectaccessreviews.authorization.k8s.io
kube-test-expected-false patch selfsubjectaccessreviews.authorization.k8s.io
kube-test-expected-false delete selfsubjectaccessreviews.authorization.k8s.io

heading "selfsubjectrulesreviews tests"

test-task "An Experience Developer should only be able to create selfsubjectrulesreviews"
echo

kube-test-expected-false get selfsubjectrulesreviews.authorization.k8s.io
kube-test-expected-false list selfsubjectrulesreviews.authorization.k8s.io
kube-test-expected-false watch selfsubjectrulesreviews.authorization.k8s.io
kube-test-expected-true create selfsubjectrulesreviews.authorization.k8s.io
kube-test-expected-false update selfsubjectrulesreviews.authorization.k8s.io
kube-test-expected-false patch selfsubjectrulesreviews.authorization.k8s.io
kube-test-expected-false delete selfsubjectrulesreviews.authorization.k8s.io

heading "pods tests"

test-task "An Experience Developer should be able to get, list, watch and delete pods"
echo

kube-test-expected-true get pods
kube-test-expected-true list pods
kube-test-expected-true watch pods
kube-test-expected-false create pods
kube-test-expected-false update pods
kube-test-expected-false patch pods
kube-test-expected-false delete pods

heading "configmaps tests"

test-task "An Experience Developer should be able to get, list and watch configmaps"
echo

kube-test-expected-true get configmaps
kube-test-expected-true list configmaps
kube-test-expected-true watch configmaps
kube-test-expected-false create configmaps
kube-test-expected-false update configmaps
kube-test-expected-false patch configmaps
kube-test-expected-false delete configmaps

heading "deployments tests"

test-task "An Experience Developer should be able to get, list and watch deployments"
echo

kube-test-expected-true get deployments
kube-test-expected-true list deployments
kube-test-expected-true watch deployments
kube-test-expected-false create deployments
kube-test-expected-false update deployments
kube-test-expected-false patch deployments
kube-test-expected-false delete deployments

heading "events tests"

test-task "An Experience Developer should be able to get, list and watch events"
echo

kube-test-expected-false get events
kube-test-expected-false list events
kube-test-expected-false watch events
kube-test-expected-false create events
kube-test-expected-false update events
kube-test-expected-false patch events
kube-test-expected-false delete events

heading "namespaces tests"

test-task "An Experience Developer should be able to get, list and watch namespaces"
echo

kube-test-expected-true get namespaces
kube-test-expected-true list namespaces
kube-test-expected-true watch namespaces
kube-test-expected-false create namespaces
kube-test-expected-false update namespaces
kube-test-expected-false patch namespaces
kube-test-expected-false delete namespaces

heading "pods log tests"

test-task "An Experience Developer should be able to get, list and watch pod logs"
echo

kube-test-expected-true get pods log
kube-test-expected-true list pods log
kube-test-expected-true watch pods log
kube-test-expected-false create pods log
kube-test-expected-false update pods log
kube-test-expected-false patch pods log
kube-test-expected-false delete pods log

heading "services tests"

test-task "An Experience Developer should be able to get, list and watch services"
echo

kube-test-expected-true get service
kube-test-expected-true list service
kube-test-expected-true watch service
kube-test-expected-false create service
kube-test-expected-false update service
kube-test-expected-false patch service
kube-test-expected-false delete service

heading "[/api/*]"

test-task "An Experience Developer should only be able to get the endpoint /api/*"
echo

kube-test-expected-true get /api/*
kube-test-expected-false list /api/*
kube-test-expected-false watch /api/*
kube-test-expected-false create /api/*
kube-test-expected-false update /api/*
kube-test-expected-false patch /api/*
kube-test-expected-false delete /api/*

heading "[/api] tests"

test-task "An Experience Developer should only be able to get the endpoint /api"
echo

kube-test-expected-true get /api
kube-test-expected-false list /api
kube-test-expected-false watch /api
kube-test-expected-false create /api
kube-test-expected-false update /api
kube-test-expected-false patch /api
kube-test-expected-false delete /api

heading "[/apis/*] tests"

test-task "An Experience Developer should only be able to get the endpoint /apis/*"
echo

kube-test-expected-true get /apis/*
kube-test-expected-false list /apis/*
kube-test-expected-false watch /apis/*
kube-test-expected-false create /apis/*
kube-test-expected-false update /apis/*
kube-test-expected-false patch /apis/*
kube-test-expected-false delete /apis/*

heading "[/apis] tests"

test-task "An Experience Developer should only be able to get the endpoint /apis"
echo

kube-test-expected-true get /apis
kube-test-expected-false list /apis
kube-test-expected-false watch /apis
kube-test-expected-false create /apis
kube-test-expected-false update /apis
kube-test-expected-false patch /apis
kube-test-expected-false delete /apis

heading "[/healthz] tests"

test-task "An Experience Developer should only be able to get the endpoint /healthz"
echo

kube-test-expected-true get /healthz
kube-test-expected-false list /healthz
kube-test-expected-false watch /healthz
kube-test-expected-false create /healthz
kube-test-expected-false update /healthz
kube-test-expected-false patch /healthz
kube-test-expected-false delete /healthz

heading "[/livez] tests"

test-task "An Experience Developer should only be able to get the endpoint /livez"
echo

kube-test-expected-true get /livez
kube-test-expected-false list /livez
kube-test-expected-false watch /livez
kube-test-expected-false create /livez
kube-test-expected-false update /livez
kube-test-expected-false patch /livez
kube-test-expected-false delete /livez

heading "[/openapi/*] tests"

test-task "An Experience Developer should only be able to get the endpoint /openapi/*"
echo

kube-test-expected-true get /openapi/*
kube-test-expected-false list /openapi/*
kube-test-expected-false watch /openapi/*
kube-test-expected-false create /openapi/*
kube-test-expected-false update /openapi/*
kube-test-expected-false patch /openapi/*
kube-test-expected-false delete /openapi/*

heading "[/openapi] tests"

test-task "An Experience Developer should only be able to get the endpoint /openapi"
echo

kube-test-expected-true get /openapi
kube-test-expected-false list /openapi
kube-test-expected-false watch /openapi
kube-test-expected-false create /openapi
kube-test-expected-false update /openapi
kube-test-expected-false patch /openapi
kube-test-expected-false delete /openapi

heading "[/readyz] tests"

test-task "An Experience Developer should only be able to get the endpoint /readyz"
echo

kube-test-expected-true get /readyz
kube-test-expected-false list /readyz
kube-test-expected-false watch /readyz
kube-test-expected-false create /readyz
kube-test-expected-false update /readyz
kube-test-expected-false patch /readyz
kube-test-expected-false delete /readyz

heading "[/version/] tests"

test-task "An Experience Developer should only be able to get the endpoint /version/"
echo

kube-test-expected-true get /version/
kube-test-expected-false list /version/
kube-test-expected-false watch /version/
kube-test-expected-false create /version/
kube-test-expected-false update /version/
kube-test-expected-false patch /version/
kube-test-expected-false delete /version/

heading "[/version] tests"

test-task "An Experience Developer should only be able to get the endpoint /version"
echo

kube-test-expected-true get /version
kube-test-expected-false list /version
kube-test-expected-false watch /version
kube-test-expected-false create /version
kube-test-expected-false update /version
kube-test-expected-false patch /version
kube-test-expected-false delete /version

heading "Current Permissions List"

kubectl auth can-i --list --namespace=${namespace} --as AHB-NonProd-AKS-Experience-Developer --as-group ${groupID}
echo
echo

printf "Test Summary:\n"
test-summary
echo
pass-fail-total
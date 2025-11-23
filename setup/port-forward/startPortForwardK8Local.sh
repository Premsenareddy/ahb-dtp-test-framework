#!/usr/bin/env bash

export NAMESPACE=$1
export CLUSTER="ahb-aen-npd-dbp-alpha-prv-aks-1"
echo "Port forwarding to ${NAMESPACE}"


    kubectl port-forward svc/alpha-authorization-adapter 8113:8080 -n obp-dev --context $CLUSTER &

    kubectl port-forward svc/alpha-authentication-adapter 8110:8080 -n obp-dev --context $CLUSTER  &

    kubectl port-forward svc/alpha-certificate-service 8138:8080 -n obp-dev --context $CLUSTER  &
    
    kubectl port-forward svc/alpha-otp-service 8108:8080 -n obp-dev --context $CLUSTER  &

    kubectl port-forward svc/alpha-development-simulator-service 8199:8080 -n obp-dev --context $CLUSTER  &


    kubectl port-forward svc/alpha-customer-adapter 8102:8080 -n ${NAMESPACE} &

    kubectl port-forward svc/alpha-banking-ops-service 8115:8080 -n ${NAMESPACE} &

    kubectl port-forward svc/alpha-payment-service 8120:8080 -n ${NAMESPACE} &


    # kubectl port-forward svc/alpha-bank-dev-web-portal 8299:8080 -n ${NAMESPACE} &

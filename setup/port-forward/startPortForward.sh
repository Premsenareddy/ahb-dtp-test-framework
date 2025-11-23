#!/usr/bin/env bash

export NAMESPACE=$1
echo "Port forwarding to ${NAMESPACE}"



    kubectl port-forward svc/alpha-authentication-adapter 8110:8080 -n ${NAMESPACE} &

    kubectl port-forward svc/alpha-otp-service 8108:8080 -n ${NAMESPACE} &

    kubectl port-forward svc/alpha-customer-adapter 8102:8080 -n ${NAMESPACE} &

    kubectl port-forward svc/alpha-certificate-service 8138:8080 -n ${NAMESPACE} &

    kubectl port-forward svc/alpha-payment-service 8120:8080 -n ${NAMESPACE} &

    kubectl port-forward svc/alpha-temenos-banking-adapter 8126:8080 -n ${NAMESPACE} &

    kubectl port-forward svc/alpha-development-simulator-service 8199:8080 -n ${NAMESPACE} &

    kubectl port-forward svc/ahb-sanctions-aml-adapter 8125:8080 -n ${NAMESPACE} &

    kubectl port-forward svc/alpha-email-notification-adapter 8129:8080 -n ${NAMESPACE} &

    kubectl port-forward svc/alpha-iban-adapter 8145:8080 -n ${NAMESPACE} &

    kubectl port-forward svc/alpha-idnow-idv-adapter 8132:8080 -n ${NAMESPACE} &

    kubectl port-forward svc/alpha-notification-adapter 8133:8080 -n ${NAMESPACE} &

    kubectl port-forward svc/alpha-document-adapter 8140:8080 -n ${NAMESPACE} &

    kubectl port-forward svc/alpha-utility-payments-adapter 8154:8080 -n ${NAMESPACE} &


#    kubectl port-forward svc/alpha-card-adapter 8101:8080 -n ${NAMESPACE} &


#    kubectl port-forward svc/alpha-onboarding-service 8103:8080 -n ${NAMESPACE} &
#    kubectl port-forward svc/alpha-banking-ops-service 8115:8080 -n ${NAMESPACE} &
#    kubectl port-forward svc/alpha-account-service 8106:8080 -n ${NAMESPACE} &

#    kubectl port-forward svc/alpha-authorization-adapter 8113:8080 -n ${NAMESPACE} &

##################### -----------------------------------------------

#    kubectl port-forward svc/alpha-mambu-banking-adapter 8104:8080 -n ${NAMESPACE} &
#    kubectl port-forward svc/alpha-banking-routing-service 8104:8080 -n ${NAMESPACE} &

# ------------------------------------------------------------------------------------


#    kubectl port-forward svc/alpha-company-info-adapter 8105:8080 -n ${NAMESPACE} &
#    kubectl port-forward svc/alpha-config-service 8109:8080 -n ${NAMESPACE} &

#    kubectl port-forward svc/alpha-transaction-service 8131:8080 -n ${NAMESPACE} &

#
#    kubectl port-forward svc/alpha-openbanking-adapter 8119:8080 -n ${NAMESPACE} &
#
#    kubectl port-forward svc/alpha-credit-onboarding-adapter 8123:8080 -n ${NAMESPACE} &
#
#    kubectl port-forward svc/alpha-cases-adapter 8140:8080 -n ${NAMESPACE} &
#
#    kubectl port-forward svc/alpha-ezbob-adapter 8139:8080 -n ${NAMESPACE} &
#
#    kubectl port-forward svc/alpha-flowable-admin 8080:8080 -n ${NAMESPACE} &
#
#    kubectl port-forward svc/elasticsearch-master 9200:9200 -n elastic-search


#    kubectl port-forward svc/openam -n forgerock-test 8299:8080 &
#done


#kubectl port-forward -n ${NAMESPACE}   $(kubectl get pod -n ${NAMESPACE} -l app=jaeger \
#    -o jsonpath='{.items[0].metadata.name}') 16690:16686 &
#
#kubectl port-forward -n ${NAMESPACE}   $(kubectl get pod -n ${NAMESPACE} -l app=jaeger \
#    -o jsonpath='{.items[0].metadata.name}') 9411:9411 &
#
#kubectl port-forward -n istio-system   $(kubectl get pod -n istio-system -l app=jaeger \
#    -o jsonpath='{.items[0].metadata.name}') 16691:16686
#!/usr/bin/env bash

export IMG_TAG_TO_DEPLOY=ahb-dtp-test-mockserver-$1
docker build -t $IMG_TAG_TO_DEPLOY  .

envsubst < k8s-resources.yaml > k8s-resources.generated.local.yaml

kubectl apply -f k8s-resources.generated.local.yaml -n obp-local

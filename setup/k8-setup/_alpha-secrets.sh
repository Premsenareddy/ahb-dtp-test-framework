#!/usr/bin/env bash

## =====================================================================================================================
## Internal APIs
## =====================================================================================================================

# is this right (naming convention) ???
vault kv put internal/alpha/dev/auth/api/key ttl=5m key="8d6153d6-9303-4efd-b093-c3196fbcc462"

vault kv put internal/alpha/dev/authentication-adapter/api/key ttl=5m key="c631a51b-9227-48b7-ac76-f60649df59fd"

vault kv put internal/alpha/dev/authorization-adapter/api/key ttl=5m key="8d6153d6-9303-4efd-b093-c3196fbcc462"

vault kv put internal/alpha/dev/banking/api/key ttl=5m key="8d6153d6-9303-4efd-b093-c3196fbcc462"

vault kv put internal/alpha/dev/banking-routing-service/api/key ttl=5m key="c631a51b-9227-48b7-ac76-f60649df59fd"

vault kv put internal/alpha/dev/card/api/key ttl=5m key="c631a51b-9227-48b7-ac76-f60649df59fd"
vault kv put internal/alpha/dev/card/api/basic-auth ttl=5m username="alpha-card-adapter-webhook-user" password="E58bb91c-dfb7-4c1b-9877-1d22418e83d7"

vault kv put internal/alpha/dev/certificate-service/api/key ttl=5m key="c631a51b-9227-48b7-ac76-f60649df59fd"

vault kv put internal/alpha/dev/customer/api/key ttl=5m key="c631a51b-9227-48b7-ac76-f60649df59fd"

vault kv put internal/alpha/dev/core-banking-adapter/api/key ttl=5m key="c631a51b-9227-48b7-ac76-f60649df59fd"

vault kv put internal/alpha/dev/payment-service/api/key ttl=5m key="c631a51b-9227-48b7-ac76-f60649df59fd"

## =====================================================================================================================
## 3rd Party APIs
## =====================================================================================================================

vault kv put internal/alpha/dev/card/marqeta/credentials ttl=5m \
  username="user58641551873857" \
  password="0688a8d1-4b65-4bd5-b06b-6a7eee0e4e81" \
  funding-token="62f2ecb8-05e1-4a01-91b0-e2c3088e7fca" \
  product-token="142fb881-fd59-419c-9e8d-6240be41a40d"

## Forgerock
vault kv put internal/alpha/dev/card/forgerock/credentials ttl=5m \
  client-id="78a1df05" \
  client-secret="c5aa994a6e1dd2480d96a3e5aa6ca2ac" \
  username="amadmin" \
  password="Password123"

## =====================================================================================================================
## Database
## =====================================================================================================================

vault kv put internal/alpha/dev/database ttl=5m username="alpha" password="alpha"

## =====================================================================================================================
## Redis
## =====================================================================================================================

vault kv put internal/alpha/dev/redis ttl=5m password="alpha"

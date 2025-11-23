storage "postgresql" {
  connection_url = "postgres://alpha:alpha@postgres.obp-local.svc.cluster.local:5432/vault?sslmode=disable"
}
disable_mlock = true
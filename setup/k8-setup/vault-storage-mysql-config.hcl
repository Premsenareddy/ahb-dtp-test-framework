storage "mysql" {
  address  = "mysql.obp-local.svc.cluster.local:3306"
  database = "vault"
  username = "alpha"
  password = "alpha"
}
disable_mlock = true
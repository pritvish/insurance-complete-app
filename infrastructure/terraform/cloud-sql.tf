# ─── Cloud SQL — one PostgreSQL 15 instance per service ──────────────────────
# Each service has a dedicated instance for strict isolation.
# In prod, Tier-0 services get db-custom-4-15360; others get db-custom-2-7680.

locals {
  tier0_services = toset([
    "customer", "policy", "claims", "payment"
  ])

  tier1_services = toset([
    "premiumcalc", "billing", "fraud", "referencedata",
    "broker", "workflow"
  ])

  tier2_services = toset([
    "document", "notification", "audit", "analytics"
  ])

  all_services = merge(
    { for s in local.tier0_services  : s => "db-custom-4-15360" },
    { for s in local.tier1_services  : s => "db-custom-2-7680"  },
    { for s in local.tier2_services  : s => "db-custom-2-4096"  }
  )

  db_names = {
    customer    = "customer_db"
    policy      = "policy_db"
    premiumcalc = "premium_calc_db"
    claims      = "claims_db"
    payment     = "payment_db"
    billing     = "billing_db"
    document    = "document_db"
    fraud       = "fraud_db"
    notification = "notification_db"
    referencedata = "reference_data_db"
    broker      = "broker_db"
    audit       = "audit_db"
    analytics   = "analytics_db"
    workflow    = "workflow_db"
  }
}

resource "google_sql_database_instance" "eip" {
  for_each         = local.all_services
  name             = "eip-${each.key}-${var.environment}"
  database_version = "POSTGRES_15"
  region           = var.region

  settings {
    tier              = var.environment == "prod" ? each.value : "db-f1-micro"
    availability_type = var.environment == "prod" ? "REGIONAL" : "ZONAL"
    disk_autoresize   = true
    disk_size         = var.environment == "prod" ? 100 : 10

    backup_configuration {
      enabled                        = true
      start_time                     = "03:00"
      point_in_time_recovery_enabled = var.environment == "prod"
      transaction_log_retention_days = var.environment == "prod" ? 7 : 1
      backup_retention_settings {
        retained_backups = var.environment == "prod" ? 30 : 7
      }
    }

    ip_configuration {
      ipv4_enabled    = false
      private_network = google_compute_network.eip_vpc.id
      require_ssl     = true
    }

    maintenance_window {
      day          = 7   # Sunday
      hour         = 4
      update_track = "stable"
    }

    database_flags {
      name  = "log_min_duration_statement"
      value = "1000"   # Log queries > 1s
    }
    database_flags {
      name  = "log_connections"
      value = "on"
    }
    database_flags {
      name  = "max_connections"
      value = var.environment == "prod" ? "200" : "100"
    }
  }

  deletion_protection = var.environment == "prod"

  depends_on = [google_service_networking_connection.private_vpc_connection]
}

resource "google_sql_database" "eip" {
  for_each = local.db_names
  name     = each.value
  instance = google_sql_database_instance.eip[each.key].name
}

resource "google_sql_user" "eip" {
  for_each = local.all_services
  name     = var.db_user
  instance = google_sql_database_instance.eip[each.key].name
  password = var.db_password
}

# ─── Outputs ─────────────────────────────────────────────────────────────────

output "cloud_sql_connection_names" {
  description = "Cloud SQL connection names for Cloud SQL Auth Proxy sidecar"
  value       = { for k, v in google_sql_database_instance.eip : k => v.connection_name }
}

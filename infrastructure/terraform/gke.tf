# ─── GKE Autopilot Cluster ───────────────────────────────────────────────────

resource "google_container_cluster" "eip" {
  provider = google-beta
  name     = "${var.cluster_name}-${var.environment}"
  location = var.region

  enable_autopilot = true

  network    = google_compute_network.eip_vpc.id
  subnetwork = google_compute_subnetwork.eip_subnet.id

  ip_allocation_policy {
    cluster_secondary_range_name  = "pods"
    services_secondary_range_name = "services"
  }

  private_cluster_config {
    enable_private_nodes    = true
    enable_private_endpoint = false
    master_ipv4_cidr_block  = "172.16.0.0/28"
  }

  release_channel {
    channel = var.environment == "prod" ? "STABLE" : "REGULAR"
  }

  workload_identity_config {
    workload_pool = "${var.project_id}.svc.id.goog"
  }

  vertical_pod_autoscaling {
    enabled = true
  }

  master_authorized_networks_config {
    cidr_blocks {
      cidr_block   = "0.0.0.0/0"
      display_name = "All (restrict in prod)"
    }
  }

  logging_config {
    enable_components = ["SYSTEM_COMPONENTS", "WORKLOADS"]
  }

  monitoring_config {
    enable_components = ["SYSTEM_COMPONENTS", "WORKLOADS"]
    managed_prometheus {
      enabled = true
    }
  }

  depends_on = [
    google_project_service.apis,
    google_compute_subnetwork.eip_subnet,
  ]
}

# ─── GKE Service Account ──────────────────────────────────────────────────────

resource "google_service_account" "gke_workload" {
  account_id   = "eip-gke-workload-${var.environment}"
  display_name = "EIP GKE Workload Identity SA — ${var.environment}"
}

resource "google_project_iam_member" "gke_workload_sql" {
  project = var.project_id
  role    = "roles/cloudsql.client"
  member  = "serviceAccount:${google_service_account.gke_workload.email}"
}

resource "google_project_iam_member" "gke_workload_secret" {
  project = var.project_id
  role    = "roles/secretmanager.secretAccessor"
  member  = "serviceAccount:${google_service_account.gke_workload.email}"
}

# ─── Outputs ─────────────────────────────────────────────────────────────────

output "gke_cluster_name" {
  value = google_container_cluster.eip.name
}

output "gke_cluster_endpoint" {
  value     = google_container_cluster.eip.endpoint
  sensitive = true
}

output "gke_get_credentials" {
  value = "gcloud container clusters get-credentials ${google_container_cluster.eip.name} --region ${var.region} --project ${var.project_id}"
}

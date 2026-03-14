terraform {
  required_version = ">= 1.6.0"

  required_providers {
    google = {
      source  = "hashicorp/google"
      version = "~> 5.10"
    }
    google-beta = {
      source  = "hashicorp/google-beta"
      version = "~> 5.10"
    }
  }

  backend "gcs" {
    bucket = "eip-terraform-state"
    prefix = "insurance-platform"
  }
}

provider "google" {
  project = var.project_id
  region  = var.region
}

provider "google-beta" {
  project = var.project_id
  region  = var.region
}

# ─── Enable required APIs ─────────────────────────────────────────────────────

resource "google_project_service" "apis" {
  for_each = toset([
    "container.googleapis.com",
    "sqladmin.googleapis.com",
    "redis.googleapis.com",
    "secretmanager.googleapis.com",
    "servicenetworking.googleapis.com",
    "cloudresourcemanager.googleapis.com",
    "iam.googleapis.com",
    "monitoring.googleapis.com",
    "logging.googleapis.com",
  ])
  service            = each.value
  disable_on_destroy = false
}

# ─── VPC Network ─────────────────────────────────────────────────────────────

resource "google_compute_network" "eip_vpc" {
  name                    = "eip-vpc-${var.environment}"
  auto_create_subnetworks = false
  depends_on              = [google_project_service.apis]
}

resource "google_compute_subnetwork" "eip_subnet" {
  name          = "eip-subnet-${var.environment}"
  ip_cidr_range = "10.0.0.0/20"
  region        = var.region
  network       = google_compute_network.eip_vpc.id

  secondary_ip_range {
    range_name    = "pods"
    ip_cidr_range = "10.1.0.0/16"
  }
  secondary_ip_range {
    range_name    = "services"
    ip_cidr_range = "10.2.0.0/20"
  }
}

# Private services access (Cloud SQL uses peering)
resource "google_compute_global_address" "private_ip_range" {
  name          = "eip-private-ip-${var.environment}"
  purpose       = "VPC_PEERING"
  address_type  = "INTERNAL"
  prefix_length = 16
  network       = google_compute_network.eip_vpc.id
}

resource "google_service_networking_connection" "private_vpc_connection" {
  network                 = google_compute_network.eip_vpc.id
  service                 = "servicenetworking.googleapis.com"
  reserved_peering_ranges = [google_compute_global_address.private_ip_range.name]
  depends_on              = [google_project_service.apis]
}

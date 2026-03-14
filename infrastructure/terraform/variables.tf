variable "project_id" {
  description = "GCP project ID"
  type        = string
}

variable "region" {
  description = "GCP region for all resources"
  type        = string
  default     = "us-central1"
}

variable "environment" {
  description = "Deployment environment (dev | staging | prod)"
  type        = string
  validation {
    condition     = contains(["dev", "staging", "prod"], var.environment)
    error_message = "environment must be dev, staging, or prod."
  }
}

variable "cluster_name" {
  description = "GKE cluster name"
  type        = string
  default     = "eip-cluster"
}

variable "db_password" {
  description = "Master password for all Cloud SQL instances"
  type        = string
  sensitive   = true
}

variable "db_user" {
  description = "Database user for all Cloud SQL instances"
  type        = string
  default     = "eip_user"
}

variable "gke_node_count" {
  description = "Initial node count per zone for GKE node pool"
  type        = number
  default     = 2
}

variable "gke_machine_type" {
  description = "Machine type for GKE nodes"
  type        = string
  default     = "e2-standard-4"
}

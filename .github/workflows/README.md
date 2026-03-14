# GitHub Actions Workflows

## Workflows

| File | Trigger | Purpose |
|---|---|---|
| `ci.yml` | PR to `main`/`develop` | Build, test, validate infra |
| `cd.yml` | Push to `main` / Release / Manual | Build images, deploy to GKE |

## Required GitHub Secrets

Configure these under **Settings → Secrets and variables → Actions**:

| Secret | Description |
|---|---|
| `GCP_PROJECT_ID` | GCP project ID (e.g. `my-eip-project`) |
| `GCP_SA_KEY` | GCP Service Account JSON key (base64-encoded) |

## Required GitHub Environments

Configure these under **Settings → Environments**:

| Environment | Protection rules |
|---|---|
| `dev` | No approval required — auto-deploys on merge to `main` |
| `staging` | Required reviewer: 1 person |
| `prod` | Required reviewers: 2 people, wait timer: 5 minutes |

## CD Pipeline flow

```
Push to main
    │
    ├─ build (Maven package → JARs)
    │
    ├─ docker (matrix: 14 services → GCR push)
    │
    └─ deploy-dev (auto, no approval)
         │
         └─ deploy-staging (manual approval required) ← triggered by GitHub Release
              │
              └─ deploy-prod (2 approvals + wait timer)
```

## Creating a GCP Service Account for CI/CD

```bash
# Create the SA
gcloud iam service-accounts create eip-github-actions \
  --display-name="EIP GitHub Actions"

# Grant required roles
gcloud projects add-iam-policy-binding YOUR_PROJECT_ID \
  --member="serviceAccount:eip-github-actions@YOUR_PROJECT_ID.iam.gserviceaccount.com" \
  --role="roles/container.developer"

gcloud projects add-iam-policy-binding YOUR_PROJECT_ID \
  --member="serviceAccount:eip-github-actions@YOUR_PROJECT_ID.iam.gserviceaccount.com" \
  --role="roles/storage.admin"   # for GCR push

# Create and download key
gcloud iam service-accounts keys create key.json \
  --iam-account=eip-github-actions@YOUR_PROJECT_ID.iam.gserviceaccount.com

# Base64-encode it and add to GitHub Secrets as GCP_SA_KEY
base64 -i key.json | pbcopy

# Delete local key file immediately
rm key.json
```

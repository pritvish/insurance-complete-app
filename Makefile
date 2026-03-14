.PHONY: build test docker-up docker-down kafka-topics k8s-deploy clean help

DOCKER_COMPOSE = docker compose
KUBECTL = kubectl
TERRAFORM = terraform
MAVEN = ./mvnw

# Default target
help:
	@echo "Enterprise Insurance Platform - Make targets"
	@echo ""
	@echo "  build-all          Build all services"
	@echo "  test-all           Run all tests"
	@echo "  docker-up          Start local dev environment (all infra)"
	@echo "  docker-down        Stop local dev environment"
	@echo "  docker-build       Build all Docker images"
	@echo "  kafka-topics       Create all Kafka topics"
	@echo "  k8s-deploy-dev     Deploy to dev Kubernetes cluster"
	@echo "  k8s-deploy-prod    Deploy to production Kubernetes cluster"
	@echo "  tf-plan-dev        Terraform plan for dev environment"
	@echo "  tf-apply-dev       Terraform apply for dev environment"
	@echo "  clean              Clean all build artifacts"
	@echo "  lint               Run code quality checks"

build-all:
	$(MAVEN) clean package -DskipTests

test-all:
	$(MAVEN) verify

test-unit:
	$(MAVEN) test

test-integration:
	$(MAVEN) verify -P integration

docker-up:
	$(DOCKER_COMPOSE) up -d
	@echo "Waiting for services to be healthy..."
	@sleep 30
	@$(MAKE) kafka-topics
	@echo "Dev environment ready!"

docker-down:
	$(DOCKER_COMPOSE) down

docker-build:
	$(DOCKER_COMPOSE) build

docker-rebuild:
	$(DOCKER_COMPOSE) down
	$(DOCKER_COMPOSE) build --no-cache
	$(DOCKER_COMPOSE) up -d

kafka-topics:
	@echo "Creating Kafka topics..."
	docker exec kafka-1 kafka-topics --bootstrap-server localhost:9092 --create --if-not-exists --topic customer.customers.registered --partitions 12 --replication-factor 3
	docker exec kafka-1 kafka-topics --bootstrap-server localhost:9092 --create --if-not-exists --topic customer.customers.kyc-completed --partitions 12 --replication-factor 3
	docker exec kafka-1 kafka-topics --bootstrap-server localhost:9092 --create --if-not-exists --topic customer.customers.suspended --partitions 6 --replication-factor 3
	docker exec kafka-1 kafka-topics --bootstrap-server localhost:9092 --create --if-not-exists --topic policy.policies.quoted --partitions 24 --replication-factor 3
	docker exec kafka-1 kafka-topics --bootstrap-server localhost:9092 --create --if-not-exists --topic policy.policies.bound --partitions 24 --replication-factor 3
	docker exec kafka-1 kafka-topics --bootstrap-server localhost:9092 --create --if-not-exists --topic policy.policies.issued --partitions 24 --replication-factor 3
	docker exec kafka-1 kafka-topics --bootstrap-server localhost:9092 --create --if-not-exists --topic policy.policies.endorsed --partitions 12 --replication-factor 3
	docker exec kafka-1 kafka-topics --bootstrap-server localhost:9092 --create --if-not-exists --topic policy.policies.cancelled --partitions 12 --replication-factor 3
	docker exec kafka-1 kafka-topics --bootstrap-server localhost:9092 --create --if-not-exists --topic policy.policies.renewed --partitions 12 --replication-factor 3
	docker exec kafka-1 kafka-topics --bootstrap-server localhost:9092 --create --if-not-exists --topic premium.calculations.completed --partitions 24 --replication-factor 3
	docker exec kafka-1 kafka-topics --bootstrap-server localhost:9092 --create --if-not-exists --topic claims.claims.filed --partitions 24 --replication-factor 3
	docker exec kafka-1 kafka-topics --bootstrap-server localhost:9092 --create --if-not-exists --topic claims.claims.coverage-verified --partitions 12 --replication-factor 3
	docker exec kafka-1 kafka-topics --bootstrap-server localhost:9092 --create --if-not-exists --topic claims.claims.assigned --partitions 12 --replication-factor 3
	docker exec kafka-1 kafka-topics --bootstrap-server localhost:9092 --create --if-not-exists --topic claims.claims.approved --partitions 24 --replication-factor 3
	docker exec kafka-1 kafka-topics --bootstrap-server localhost:9092 --create --if-not-exists --topic claims.claims.closed --partitions 12 --replication-factor 3
	docker exec kafka-1 kafka-topics --bootstrap-server localhost:9092 --create --if-not-exists --topic payment.payments.premium-received --partitions 24 --replication-factor 3
	docker exec kafka-1 kafka-topics --bootstrap-server localhost:9092 --create --if-not-exists --topic payment.payments.claim-paid --partitions 24 --replication-factor 3
	docker exec kafka-1 kafka-topics --bootstrap-server localhost:9092 --create --if-not-exists --topic payment.payments.failed --partitions 12 --replication-factor 3
	docker exec kafka-1 kafka-topics --bootstrap-server localhost:9092 --create --if-not-exists --topic billing.invoices.generated --partitions 12 --replication-factor 3
	docker exec kafka-1 kafka-topics --bootstrap-server localhost:9092 --create --if-not-exists --topic billing.invoices.overdue --partitions 6 --replication-factor 3
	docker exec kafka-1 kafka-topics --bootstrap-server localhost:9092 --create --if-not-exists --topic billing.commissions.calculated --partitions 6 --replication-factor 3
	docker exec kafka-1 kafka-topics --bootstrap-server localhost:9092 --create --if-not-exists --topic fraud.scores.returned --partitions 24 --replication-factor 3
	docker exec kafka-1 kafka-topics --bootstrap-server localhost:9092 --create --if-not-exists --topic fraud.cases.opened --partitions 6 --replication-factor 3
	docker exec kafka-1 kafka-topics --bootstrap-server localhost:9092 --create --if-not-exists --topic document.documents.generated --partitions 12 --replication-factor 3
	docker exec kafka-1 kafka-topics --bootstrap-server localhost:9092 --create --if-not-exists --topic document.documents.signed --partitions 12 --replication-factor 3
	docker exec kafka-1 kafka-topics --bootstrap-server localhost:9092 --create --if-not-exists --topic notification.notifications.sent --partitions 6 --replication-factor 3
	docker exec kafka-1 kafka-topics --bootstrap-server localhost:9092 --create --if-not-exists --topic workflow.sagas.step-completed --partitions 24 --replication-factor 3
	docker exec kafka-1 kafka-topics --bootstrap-server localhost:9092 --create --if-not-exists --topic workflow.sagas.compensating --partitions 24 --replication-factor 3
	docker exec kafka-1 kafka-topics --bootstrap-server localhost:9092 --create --if-not-exists --topic reference.data.updated --partitions 3 --replication-factor 3
	docker exec kafka-1 kafka-topics --bootstrap-server localhost:9092 --create --if-not-exists --topic analytics.reports.generated --partitions 6 --replication-factor 3
	docker exec kafka-1 kafka-topics --bootstrap-server localhost:9092 --create --if-not-exists --topic cdc.policy-db.policies --partitions 24 --replication-factor 3
	docker exec kafka-1 kafka-topics --bootstrap-server localhost:9092 --create --if-not-exists --topic cdc.customer-db.customers --partitions 12 --replication-factor 3
	docker exec kafka-1 kafka-topics --bootstrap-server localhost:9092 --create --if-not-exists --topic cdc.claims-db.claims --partitions 24 --replication-factor 3
	@echo "Kafka topics created!"

k8s-deploy-dev:
	$(KUBECTL) apply -k infrastructure/kubernetes/overlays/dev

k8s-deploy-staging:
	$(KUBECTL) apply -k infrastructure/kubernetes/overlays/staging

k8s-deploy-prod:
	$(KUBECTL) apply -k infrastructure/kubernetes/overlays/prod

tf-plan-dev:
	cd infrastructure/terraform/environments/dev && $(TERRAFORM) plan

tf-apply-dev:
	cd infrastructure/terraform/environments/dev && $(TERRAFORM) apply

tf-plan-prod:
	cd infrastructure/terraform/environments/prod && $(TERRAFORM) plan

tf-apply-prod:
	cd infrastructure/terraform/environments/prod && $(TERRAFORM) apply -auto-approve

lint:
	$(MAVEN) checkstyle:check
	cd frontend/customer-portal && npm run lint
	cd frontend/broker-portal && npm run lint
	cd frontend/admin-portal && npm run lint

clean:
	$(MAVEN) clean
	find . -name "target" -type d -exec rm -rf {} + 2>/dev/null || true
	cd frontend/customer-portal && rm -rf dist node_modules 2>/dev/null || true
	cd frontend/broker-portal && rm -rf dist node_modules 2>/dev/null || true
	cd frontend/admin-portal && rm -rf dist node_modules 2>/dev/null || true

frontend-install:
	cd frontend/customer-portal && npm install
	cd frontend/broker-portal && npm install
	cd frontend/admin-portal && npm install

frontend-build:
	cd frontend/customer-portal && npm run build
	cd frontend/broker-portal && npm run build
	cd frontend/admin-portal && npm run build

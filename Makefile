db-up:
	docker compose -f docker/postgresql.yml up -d

db-down:
	docker compose -f docker/postgresql.yml down -v

redis-up:
	docker compose -f docker/redis.yml up -d

redis-down:
	docker compose -f docker/redis.yml down -v

kafka-up:
	docker compose -f docker/kafka.yml up -d

kafka-down:
	docker compose -f docker/kafka.yml down -v

monitoring-up:
	docker compose -f docker/monitoring.yml up -d

monitoring-down:
	docker compose -f docker/monitoring.yml down -v

services-up:
	docker compose -f docker/services.yml up -d

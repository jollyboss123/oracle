db-up:
	docker compose -f src/main/docker/postgresql.yml up -d

db-down:
	docker compose -f src/main/docker/postgresql.yml down -v

redis-up:
	docker compose -f src/main/docker/redis.yml up -d

redis-down:
	docker compose -f src/main/docker/redis.yml down -v

kafka-up:
	docker compose -f src/main/docker/kafka.yml up -d

kafka-down:
	docker compose -f src/main/docker/kafka.yml down -v

services-up:
	docker compose -f src/main/docker/services.yml up -d

#!/bin/bash

# Lab Phase 1 — start 5 Spring Boot (MongoDB chạy Docker, map :27017)
# Thứ tự: Auth → Product → Notify → Order → Gateway

set -euo pipefail

ROOT="$(cd "$(dirname "$0")" && pwd)"
cd "$ROOT"

echo "Starting ecommerce microservices (Phase 1)..."
echo "================================================"

RED='\033[0;31m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m'

check_port() {
    local port=$1
    if lsof -nP -iTCP:"$port" -sTCP:LISTEN >/dev/null 2>&1; then
        echo -e "${RED}Port $port dang duoc dung${NC}"
        return 1
    fi
    return 0
}

mongo_port_open() {
    if command -v nc >/dev/null 2>&1; then
        nc -z 127.0.0.1 27017 >/dev/null 2>&1
        return $?
    fi
    bash -c 'echo >/dev/tcp/127.0.0.1/27017' >/dev/null 2>&1
}

wait_for_service() {
    local service_name=$1
    local port=$2
    local log_file=$3
    local max_attempts=45
    local attempt=1

    echo -e "${YELLOW}Cho $service_name :$port ...${NC}"

    while [ $attempt -le $max_attempts ]; do
        if curl -sf "http://localhost:${port}/actuator/health" >/dev/null 2>&1; then
            echo -e "${GREEN}OK $service_name${NC}"
            return 0
        fi
        echo -n "."
        sleep 2
        attempt=$((attempt + 1))
    done

    echo -e "${RED}Loi: $service_name khong len sau $((max_attempts * 2))s${NC}"
    if [ -n "${log_file:-}" ] && [ -f "$log_file" ]; then
        echo "Xem log: $log_file"
        tail -n 40 "$log_file" || true
    fi
    return 1
}

echo -e "${BLUE}Kiem tra moi truong...${NC}"

if ! command -v java >/dev/null 2>&1; then
    echo -e "${RED}Chua cai Java${NC}"
    exit 1
fi

if ! command -v mvn >/dev/null 2>&1; then
    echo -e "${RED}Chua cai Maven${NC}"
    exit 1
fi

# MongoDB lab: container Docker map localhost:27017 (khong can mongosh tren may)
echo -e "${BLUE}Kiem tra MongoDB (Docker / :27017)...${NC}"
if mongo_port_open; then
    echo -e "${GREEN}MongoDB dang lang nghe localhost:27017${NC}"
    if command -v docker >/dev/null 2>&1; then
        docker ps --format '{{.Names}}  {{.Ports}}' 2>/dev/null | grep -E '27017' || true
    fi
else
    echo -e "${RED}MongoDB chua mo cong 27017${NC}"
    echo "Lab dung Mongo trong Docker. Bat container (user/pass giong application.properties):"
    echo ""
    echo "  docker start mongo-t3h 2>/dev/null || docker run -d --name mongo-t3h -p 27017:27017 \\"
    echo "    -e MONGO_INITDB_ROOT_USERNAME=root \\"
    echo "    -e MONGO_INITDB_ROOT_PASSWORD=DBVWiYdDoMnfWmK \\"
    echo "    mongo:7"
    echo ""
    echo "Sau do chay lai ./start-all.sh"
    exit 1
fi

echo -e "${GREEN}OK moi truong${NC}"

echo -e "${BLUE}Kiem tra port Spring Boot...${NC}"
for port in 8080 8081 8082 8083 8084; do
    if ! check_port "$port"; then
        echo "Tat process dang giu port $port (hoac ./stop-all.sh) roi chay lai"
        exit 1
    fi
done
echo -e "${GREEN}OK port 8080-8084 trong${NC}"

LOG_DIR="$ROOT/logs"
mkdir -p "$LOG_DIR"
echo -e "${BLUE}Start service (log: $LOG_DIR/ms-*.log)...${NC}"

echo -e "${YELLOW}Auth :8081${NC}"
(cd "$ROOT/auth-service" && mvn spring-boot:run) >"$LOG_DIR/ms-auth.log" 2>&1 &
AUTH_PID=$!
wait_for_service "Auth" 8081 "$LOG_DIR/ms-auth.log"

echo -e "${YELLOW}Product :8082${NC}"
(cd "$ROOT/product-service" && mvn spring-boot:run) >"$LOG_DIR/ms-product.log" 2>&1 &
PRODUCT_PID=$!
wait_for_service "Product" 8082 "$LOG_DIR/ms-product.log"

echo -e "${YELLOW}Notification :8084${NC}"
(cd "$ROOT/notification-service" && mvn spring-boot:run) >"$LOG_DIR/ms-notify.log" 2>&1 &
NOTIFICATION_PID=$!
wait_for_service "Notification" 8084 "$LOG_DIR/ms-notify.log"

echo -e "${YELLOW}Order :8083${NC}"
(cd "$ROOT/order-service" && mvn spring-boot:run) >"$LOG_DIR/ms-order.log" 2>&1 &
ORDER_PID=$!
wait_for_service "Order" 8083 "$LOG_DIR/ms-order.log"

echo -e "${YELLOW}API Gateway :8080${NC}"
(cd "$ROOT/api-gateway" && mvn spring-boot:run) >"$LOG_DIR/ms-gateway.log" 2>&1 &
GATEWAY_PID=$!
wait_for_service "API Gateway" 8080 "$LOG_DIR/ms-gateway.log"

{
    echo "GATEWAY_PID=$GATEWAY_PID"
    echo "AUTH_PID=$AUTH_PID"
    echo "PRODUCT_PID=$PRODUCT_PID"
    echo "ORDER_PID=$ORDER_PID"
    echo "NOTIFICATION_PID=$NOTIFICATION_PID"
} > "$ROOT/.pids"

echo "================================================"
echo -e "${GREEN}Da start 5 service${NC}"
echo ""
echo "Gateway:      http://localhost:8080"
echo "Auth:         http://localhost:8081"
echo "Product:      http://localhost:8082"
echo "Order:        http://localhost:8083"
echo "Notify:       http://localhost:8084"
echo ""
echo "Tai khoan: user@example.com / user123"
echo "           admin@example.com / admin123"
echo ""
echo "Log:"
echo "  $LOG_DIR/ms-auth.log"
echo "  $LOG_DIR/ms-product.log"
echo "  $LOG_DIR/ms-notify.log"
echo "  $LOG_DIR/ms-order.log"
echo "  $LOG_DIR/ms-gateway.log"
echo "Stop: ./stop-all.sh  (khong dung container Mongo)"
echo "================================================"

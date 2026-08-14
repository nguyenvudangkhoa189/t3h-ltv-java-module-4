#!/bin/bash

# Lab Phase 1 — stop 5 Spring Boot (giu MongoDB Docker)

set -e

ROOT="$(cd "$(dirname "$0")" && pwd)"
cd "$ROOT"

echo "Stopping ecommerce microservices..."
echo "================================================"

RED='\033[0;31m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m'

stop_by_port() {
    local service_name=$1
    local port=$2

    echo -e "${YELLOW}Stop $service_name :$port ...${NC}"

    local pids
    pids=$(lsof -ti:"$port" 2>/dev/null || true)

    if [ -n "$pids" ]; then
        echo "$pids" | xargs kill -TERM 2>/dev/null || true
        sleep 3
        local still_running
        still_running=$(lsof -ti:"$port" 2>/dev/null || true)
        if [ -n "$still_running" ]; then
            echo "$still_running" | xargs kill -9 2>/dev/null || true
        fi
        echo -e "${GREEN}OK $service_name${NC}"
    else
        echo -e "${BLUE}$service_name khong chay${NC}"
    fi
}

stop_by_port "API Gateway" 8080
stop_by_port "Order Service" 8083
stop_by_port "Notification Service" 8084
stop_by_port "Product Service" 8082
stop_by_port "Auth Service" 8081

if [ -f ".pids" ]; then
    rm -f .pids
fi

echo "================================================"
echo -e "${GREEN}Da tat 5 service Spring Boot${NC}"
echo "MongoDB Docker (cong 27017) giu nguyen — khong stop container."
echo "Start lai: ./start-all.sh"
echo "================================================"

#!/usr/bin/env bash
# Import CSV mymoviedb vào Mongo đang chạy trong Docker Compose (Lab 2).
# Ôn ý nghĩa CSV: Module 3 Bài 7 — bài này chỉ đổi "host" thành container.
#
# Cách dùng (từ thư mục demo-bai7-docker/lab2, sau docker compose up):
#   ./scripts/import-movies.sh
#   ./scripts/import-movies.sh path/to/mymoviedb.csv

set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
CSV_FILE="${1:-$ROOT_DIR/java-springboot-bai7/sample-data/mymoviedb.csv}"
DB_NAME="db_java_t3h_module3_bai_7"
MONGO_USER="root"
MONGO_PASS="DBVWiYdDoMnfWmK"

if [[ ! -f "$CSV_FILE" ]]; then
  echo "Không tìm thấy file: $CSV_FILE"
  exit 1
fi

# --- Tìm tên container service mongo (Compose đặt prefix project) ---
MONGO_CID="$(docker compose -f "$ROOT_DIR/docker-compose.yml" ps -q mongo)"
if [[ -z "$MONGO_CID" ]]; then
  echo "Chưa thấy container mongo. Chạy: cd $ROOT_DIR && docker compose up -d mongo"
  exit 1
fi

echo "Copy CSV vào container rồi mongoimport → ${DB_NAME}.mymoviedb ..."
docker cp "$CSV_FILE" "${MONGO_CID}:/tmp/mymoviedb.csv"

# Image mongo:7 có mongoimport
docker exec "$MONGO_CID" mongoimport \
  --username "$MONGO_USER" \
  --password "$MONGO_PASS" \
  --authenticationDatabase admin \
  --db "$DB_NAME" \
  --collection mymoviedb \
  --type csv \
  --headerline \
  --drop \
  --file /tmp/mymoviedb.csv

echo "Done. Đếm document:"
docker exec "$MONGO_CID" mongosh \
  --username "$MONGO_USER" \
  --password "$MONGO_PASS" \
  --authenticationDatabase admin \
  --quiet \
  --eval "db.getSiblingDB('${DB_NAME}').mymoviedb.countDocuments()"

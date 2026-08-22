#!/usr/bin/env bash
# Import CSV khi chạy Mongo trên MÁY (không qua Compose).
# Khi dùng Docker Compose Lab 2 → dùng script ở gốc demo:
#   ../scripts/import-movies.sh   (từ thư mục này) hoặc
#   demo-bai7-docker/scripts/import-movies.sh

set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
CSV_FILE="${1:-$SCRIPT_DIR/../sample-data/mymoviedb.csv}"
DB_NAME="db_java_t3h_module3_bai_7"

if [[ ! -f "$CSV_FILE" ]]; then
  echo "Không tìm thấy file: $CSV_FILE"
  exit 1
fi

echo "Importing $CSV_FILE -> ${DB_NAME}.mymoviedb (localhost) ..."
mongoimport \
  --uri "mongodb://root:DBVWiYdDoMnfWmK@localhost:27017/${DB_NAME}?authSource=admin" \
  --collection mymoviedb \
  --type csv \
  --headerline \
  --drop \
  --file "$CSV_FILE"

echo "Done. Kiểm tra: mongosh \"$DB_NAME\" --eval 'db.mymoviedb.countDocuments()'"

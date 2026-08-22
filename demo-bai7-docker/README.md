# Demo Bài 7 — Docker (Lab 1 + Lab 2)

Thư mục gốc demo cho syllabus [`6_java_m4_bai7_Docker.md`](../syllabus/module-4/6_java_m4_bai7_Docker.md).

**Hai project con — làm đúng thứ tự:**

| Lab | Folder | Mục đích |
|-----|--------|----------|
| **Lab 1** | [`lab1/`](./lab1/) | API tĩnh, không DB → Dockerfile 1 stage, `build` / `run` |
| **Lab 2** | [`lab2/`](./lab2/) | Copy Mini Project M3 Bài 10 → multi-stage + Compose |

```
demo-bai7-docker/
├── README.md                 ← file này
├── lab1/                     ← Lab 1
│   ├── README.md
│   └── java-springboot-hello/
└── lab2/                     ← Lab 2
    ├── README.md
    ├── docker-compose.yml              ← TH1: chưa có Mongo
    ├── docker-compose.host-mongo.yml   ← TH2: đã có Mongo
    ├── scripts/import-movies.sh
    └── java-springboot-bai7/
```

## Chạy nhanh

```bash
# Lab 1
cd lab1/java-springboot-hello
./mvnw package -DskipTests
docker build -t hello-docker:1.0 .
docker run -d --rm --name hello-lab1 -p 8080:8080 hello-docker:1.0
# -d = chạy nền (tắt Terminal không tắt container)
# --rm = tự xoá container khi docker stop

# Lab 2 (chọn 1)
# TH1 — chưa có Mongo:
cd lab2 && docker compose up --build -d && ./scripts/import-movies.sh
# TH2 — đã có Mongo trên máy:
# cd lab2 && docker compose -f docker-compose.host-mongo.yml up --build -d
```

Chi tiết từng lab: xem README trong `lab1/` và `lab2/`.

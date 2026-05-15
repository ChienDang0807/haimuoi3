# Deploy Haimuoi3 lên Railway

Chỉ cần tạo services, **link database**, và **điền biến** — không sửa code khi đổi URL.

## Kiến trúc

| Service | Root directory | Builder |
|---------|----------------|---------|
| Backend | `haimuoi3` | Dockerfile |
| Frontend | `haimuoi3-frontend` | Nixpacks (`railway.toml`) |
| Postgres | Plugin | Link vào Backend |
| MongoDB | Atlas hoặc Railway Mongo | Connection string vào Backend |

Monorepo git: đặt **Root Directory** đúng từng service trong Railway UI.

---

## 1. Postgres

1. Add **PostgreSQL** plugin vào project.
2. Trên service **Backend** → Variables → **Add Reference** từ Postgres (hoặc copy `DATABASE_URL`).

Backend tự parse `DATABASE_URL` (`postgresql://...`) khi `SPRING_PROFILES_ACTIVE=prod`.

**Hoặc** map tay (nếu không dùng `DATABASE_URL`):

- `PGHOST`, `PGPORT`, `PGUSER`, `PGPASSWORD`, `PGDATABASE` (Railway reference)

---

## 2. MongoDB

App **bắt buộc** có Mongo (products, cart).

Trên Backend, set **một** trong các biến:

- `MONGODB_URI` — khuyến nghị (Atlas / Railway Mongo)
- `MONGO_URL` — alias
- Hoặc `MONGODB_HOST` + `MONGODB_PORT` + `MONGODB_DATABASE`

### Tên database trong Mongo URI (bắt buộc)

Spring cần **tên database** — gắn **trên cùng chuỗi URI**, sau host/port, **trước** `?`:

```text
mongodb://user:pass@host:27017/haimuoi3-backend
mongodb+srv://user:pass@cluster.mongodb.net/haimuoi3-backend?retryWrites=true
```

**Sai** (Railway hay cấp thiếu → `Database name must not be empty`):

```text
mongodb://user:pass@host:27017
mongodb+srv://user:pass@cluster.mongodb.net/?retryWrites=true
```

**Cách sửa nhanh:** copy `MONGODB_URI` từ Railway, thêm `/haimuoi3-backend` (hoặc tên DB bạn dùng) ngay trước dấu `?` nếu có.

**Hoặc** giữ URI như Railway cấp và thêm biến:

```text
MONGODB_DATABASE=haimuoi3-backend
```

(app đọc `spring.data.mongodb.database` khi URI không có path — cần bản config mới nhất.)

---

## 3. Backend — biến bắt buộc

| Biến | Ví dụ / ghi chú |
|------|------------------|
| `SPRING_PROFILES_ACTIVE` | `prod` |
| `JWT_SECRET` | **Bắt buộc** — chuỗi random ≥ 32 ký tự (thiếu → lỗi `Could not resolve placeholder 'JWT_SECRET'`) |
| `STRIPE_SECRET_KEY` | `sk_test_...` hoặc live |
| `STRIPE_WEBHOOK_SECRET` | Stripe webhook → `https://<backend>/api/v1/payments/webhook` |
| `APP_BASE_URL` | URL public **frontend** (vd. `https://xxx.up.railway.app`) |
| `CORS_ALLOWED_ORIGINS` | Cùng URL frontend (+ `http://localhost:4200` nếu dev local) |
| `DATABASE_URL` | Từ Postgres plugin **hoặc** `PG*` / `DATABASE_*` |
| `MONGODB_URI` | Connection string Mongo |

`PORT` — Railway tự inject, không cần set.

**Health check (Railway):** `/actuator/health/liveness` — chỉ cần app đã start (port `PORT`).

Kiểm tra DB đầy đủ: `https://<backend-domain>/actuator/health` → `UP` (nếu Postgres + Mongo OK).

`PORT` — Railway inject (thường `8080`); log `Tomcat started on port 8080` là đúng.

---

## 4. Frontend — biến bắt buộc

| Biến | Ví dụ |
|------|--------|
| `API_URL` | `https://<backend-domain>/api` |

Build chạy `npm run build:railway` → ghi `environment.prod.ts` từ `API_URL`.

`PORT` — Railway tự inject cho `serve`.

---

## 5. Thứ tự deploy

1. Postgres (+ Mongo sẵn sàng)
2. **Backend** → đợi healthy
3. Copy URL public backend → set `API_URL` trên **Frontend**
4. Set `CORS_ALLOWED_ORIGINS` + `APP_BASE_URL` trên Backend = URL frontend
5. **Frontend** deploy

---

## 6. Kiểm tra nhanh

- [ ] `GET https://<be>/actuator/health` → UP
- [ ] DevTools → login không gọi `localhost:8090`
- [ ] Không lỗi CORS trên API
- [ ] Shop owner `/admin` load được

---

## Local

Xem [`.env.example`](.env.example) và `docker compose up` (postgres + mongo + backend).

# CI/CD Setup: Release APK via GitHub Actions

## Tổng quan

Workflow [`release.yml`](.github/workflows/release.yml) cho phép bạn build APK release đã ký và tạo GitHub Release chỉ bằng cách nhập **version name** và **version code** trực tiếp trên GitHub Actions.

---

## Bước 1: Lấy nội dung keystore đã mã hóa Base64

File `navi-noti-release.jks` đã được tạo trong thư mục gốc của dự án.
Mở file `keystore_base64.txt` ở thư mục gốc — đây là nội dung Base64 cần dán vào Secret.

> ⚠️ **Xoá `keystore_base64.txt` sau khi đã copy** — file này không được commit lên git (đã có trong `.gitignore`).

---

## Bước 2: Thêm Secrets vào GitHub Repository

Vào **GitHub repo → Settings → Secrets and variables → Actions → New repository secret** và thêm 4 secret sau:

| Secret name        | Giá trị                                      |
|--------------------|----------------------------------------------|
| `KEYSTORE_BASE64`  | Toàn bộ nội dung file `keystore_base64.txt`  |
| `KEYSTORE_PASSWORD`| `navinoti2026`                               |
| `KEY_ALIAS`        | `navi_noti_key`                              |
| `KEY_PASSWORD`     | `navinoti2026`                               |

> 💡 Khuyến nghị: Thay đổi password của keystore sang giá trị bảo mật hơn khi publish production.

---

## Bước 3: Cấp quyền Write cho GitHub Actions

Vào **Settings → Actions → General → Workflow permissions**:
- Chọn **Read and write permissions** ✅
- Tick **Allow GitHub Actions to create and approve pull requests** ✅

Bước này cần thiết để Actions có thể push git tag lên repo.

---

## Bước 4: Chạy workflow

1. Vào tab **Actions** → Chọn workflow **Release APK**.
2. Nhấn **Run workflow**.
3. Điền thông tin:
   - `Version name`: ví dụ `1.2.0` (theo định dạng `X.Y.Z`)
   - `Version code`: số nguyên tăng dần, ví dụ `3`
4. Nhấn **Run workflow** → Đợi khoảng 3–5 phút.

### Kết quả sau khi chạy xong:
- ✅ APK release được ký với keystore → đặt tên `navi-noti-v1.2.0.apk`
- ✅ Git tag `v1.2.0` được tạo và push lên repo
- ✅ GitHub Release `Navi Noti v1.2.0` được publish với APK đính kèm

---

## Thông tin Keystore

| Thuộc tính      | Giá trị                  |
|-----------------|--------------------------|
| File            | `navi-noti-release.jks`  |
| Alias           | `navi_noti_key`          |
| Algorithm       | RSA 2048-bit             |
| Validity        | 10,000 ngày (~27 năm)    |
| Store Password  | `navinoti2026`           |
| Key Password    | `navinoti2026`           |
| DN              | CN=NaviNoti, OU=Dyno, O=NaviNoti, L=HoChiMinh, ST=HoChiMinh, C=VN |

> 🔐 **Lưu trữ file `navi-noti-release.jks` ở nơi an toàn** — mất keystore này thì không thể update app lên Google Play.

---

## Quy ước đánh số phiên bản

| Loại thay đổi       | Ví dụ                  | versionCode |
|---------------------|------------------------|-------------|
| Patch / Bug fix     | 1.0.0 → 1.0.1          | +1          |
| Tính năng mới nhỏ  | 1.0.1 → 1.1.0          | +1          |
| Thay đổi lớn        | 1.1.0 → 2.0.0          | +1          |

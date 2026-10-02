# Kiến trúc hệ thống Navi Noti

Tài liệu này mô tả chi tiết kiến trúc, luồng xử lý và các quyết định kỹ thuật của ứng dụng **Navi Noti**, giải quyết bài toán hiển thị thông báo điều hướng Google Maps tối ưu cho đồng hồ thông minh.

---

## 1. Mục tiêu & Vấn đề giải quyết

### Vấn đề
- Thông báo điều hướng mặc định của Google Maps trên Android có độ dài văn bản lớn, thông tin phụ nhiều (ước tính thời gian ETA, cự ly tổng, chế độ giao thông).
- Smartwatch (Wear OS, Zepp OS / Amazfit, Garmin, Mi Fitness / Xiaomi Smart Band) có kích thước màn hình nhỏ và giới hạn ký tự hiển thị trên danh sách thông báo.
- Google Maps gửi cập nhật liên tục mỗi khi thay đổi khoảng cách nhỏ, làm đồng hồ rung liên tục gây hao pin và làm người dùng mất tập trung khi đang điều khiển phương tiện.

### Giải pháp của Navi Noti
1. **Súc tích hóa dữ liệu**:
   - **Tiêu đề**: Chỉ hiển thị hành động kế tiếp ngắn gọn (*Rẽ trái*, *Rẽ phải*, *Đi thẳng*, *Đi theo lối ra thứ 2*, *Đã đến nơi*).
   - **Nội dung**: Chỉ gồm khoảng cách còn lại và tên đường sắp rẽ vào (*Còn 250 m · Đường Nguyễn Huệ*). Không để tồn đọng tên đường cũ.
2. **Cơ chế rung / thông báo thông minh**:
   - Chỉ rung cảnh báo tại các mốc quyết định: khi sắp đến chỗ rẽ (~300m), gần đến chỗ rẽ (~100m), ngay điểm rẽ (<= 35m) và khi đến đích.
   - Các cập nhật khoảng cách giữa chừng được phát hành dạng thầm lặng (`setOnlyAlertOnce(true)`) để cập nhật số liệu trên màn hình đồng hồ mà không kích hoạt rung liên tục.
3. **Quản lý vòng đời chặt chẽ**:
   - Tự động hủy thông báo ngay khi người dùng dừng điều hướng hoặc đến nơi.

---

## 2. Sơ đồ kiến trúc

```
[ Google Maps App ]
        │ (Hệ thống Notification Android)
        ▼
[ GoogleMapsNotificationListenerService ]
        │
        ├──► [ GoogleMapsNotificationParser ] (Song ngữ: VI & EN)
        │         │ Bóc tách & Chuẩn hóa (Action, Distance, Street)
        │         ▼
        ├──► [ NavigationNotificationManager ]
        │         │ Quản lý Channel độ ưu tiên cao + Alert thông minh + Localized Title/Content
        │         ▼
        │   [ Smartwatch (Notification Bridge) ]
        │
        └──► [ NavigationRepository (StateFlow) ]
                  │
                  ├──► [ MainViewModel ] ──► [ HomeScreen (Compose UI) ]
                  ▲                              ▲
                  │                              │
        [ SampleJourneySimulator ]     [ PreferencesManager ]
        (Mô phỏng hành trình mẫu)      (ThemeMode & AppLanguage)
```

---

## 3. Các thành phần cốt lõi

### A. `GoogleMapsNotificationParser`
- Bóc tách văn bản từ `Notification.EXTRA_TITLE` và `Notification.EXTRA_TEXT`.
- Hỗ trợ phân tích cả tiếng Việt (*"Rẽ trái vào...", "Đi tiếp trên...", "Đi theo lối ra..."*) và tiếng Anh (*"Turn left onto...", "Continue on...", "Take exit...", "In 500 ft"*).
- Quy đổi mọi đơn vị (m, km, ft, mi) sang mét phục vụ thuật toán cảnh báo rung mốc ~300m, ~100m, điểm rẽ.

### B. `NavigationNotificationManager`
- Khởi tạo Notification Channel với `NotificationManager.IMPORTANCE_HIGH` và `VISIBILITY_PUBLIC`.
- Sử dụng ID thông báo cố định (`NOTIFICATION_ID = 1001`) để chỉ duy trì 1 thông báo duy nhất.
- Hỗ trợ xuất bản thông báo song ngữ theo ngôn ngữ cấu hình qua `step.formatTitle(context)` và `step.formatContent(context)`.

### C. `SampleJourneySimulator`
- Cung cấp kịch bản 8 bước mẫu sinh động từ điểm xuất phát, qua các khúc rẽ, cập nhật cự ly, trạng thái chờ và hoàn thành hành trình.
- Cho phép người dùng kiểm tra kết nối với smartwatch trực tiếp trong nhà mà không cần ra đường thử nghiệm.

### D. Giao diện người dùng (Jetpack Compose Material 3)
- Hỗ trợ 3 chế độ giao diện: **Hệ thống (System Default)**, **Sáng (Light)**, **Tối (Dark)**.
- Hỗ trợ 3 lựa chọn ngôn ngữ: **Hệ thống (System Default)**, **Tiếng Việt**, **English**.
- Tự động lưu cấu hình qua `PreferencesManager`.
- Mỗi file giao diện tuân thủ giới hạn tối đa 200–250 dòng và hàm tối đa 30–50 dòng.

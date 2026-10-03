# Kiến trúc hệ thống Navi Noti

Tài liệu này mô tả chi tiết kiến trúc, luồng xử lý và các quyết định kỹ thuật của ứng dụng **Navi Noti**, giải quyết bài toán hiển thị thông báo điều hướng Google Maps tối ưu cho đồng hồ thông minh.

---

## 1. Mục tiêu & Vấn đề giải quyết

### Vấn đề
- Thông báo điều hướng mặc định của Google Maps trên Android có độ dài văn bản lớn, thông tin phụ nhiều (ước tính thời gian ETA, cự ly tổng, chế độ giao thông).
- Smartwatch (Wear OS, Zepp OS / Amazfit, Garmin, Mi Fitness / Xiaomi Smart Band) có kích thước màn hình nhỏ và giới hạn ký tự hiển thị trên danh sách thông báo.
- Google Maps gửi cập nhật liên tục mỗi khi thay đổi khoảng cách nhỏ, làm đồng hồ rung liên tục gây hao pin và làm người dùng mất tập trung khi đang điều khiển phương tiện.

### Giải pháp của Navi Noti
1. **Làm rõ hành động và đường sắp đi vào**:
   - **Tiêu đề**: Hiển thị hành động kế tiếp ngắn gọn (*Rẽ trái*, *Rẽ phải*, *Đi theo lối ra thứ 2*, *Đã đến nơi*).
   - **Nội dung**: Ghép khoảng cách, hành động và tên đường đích nếu notification cung cấp (*Còn 30 m · Rẽ trái vào Đường Nguyễn Huệ*). Không để tên đường cũ tồn đọng.
2. **Cơ chế rung / thông báo thông minh**:
   - Cập nhật nội dung ngay ở bước đầu tiên; các thay đổi khoảng cách/hướng dẫn giữa các mốc được cập nhật im lặng.
   - Chỉ phát cảnh báo tại các mốc quyết định (~100m, ~30m), khi chuyển bước tới điểm rẽ và khi đến đích.
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
- Quy đổi mọi đơn vị (m, km, ft, mi) sang mét phục vụ thuật toán cảnh báo rung mốc ~100m, ~30m và điểm rẽ.

### Ranh giới nguồn dữ liệu
- Navi Noti hiện chỉ nhận được nội dung mà Google Maps đưa vào notification thông qua Android `NotificationListenerService`; trường/tên đường có thể thiếu tùy phiên bản Maps, ngôn ngữ và trạng thái điều hướng.
- Google Maps không cung cấp API Android công khai để ứng dụng bên thứ ba đọc tuyến đang điều hướng, điểm đến hoặc danh sách maneuver của phiên Maps hiện tại. Google Maps URLs chủ yếu mở Maps chứ không trả về trạng thái dẫn đường.
- Google Maps Platform Routes API có thể tính một tuyến riêng khi ứng dụng đã có origin/destination, nhưng không thể truy xuất tuyến đang chạy trong Maps. Tích hợp này cần API key, billing, quyền vị trí/đồng ý phù hợp và sẽ là một luồng sản phẩm riêng, không phải cách bổ sung dữ liệu thụ động cho demo hiện tại.
- Nếu cần dữ liệu maneuver đầy đủ và ổn định, hướng thay thế là dùng Navigation SDK để xây dựng trải nghiệm điều hướng trong Navi Noti. SDK này không đọc được phiên điều hướng đang chạy trong ứng dụng Google Maps và đòi hỏi thay đổi kiến trúc, API key/billing cùng việc tuân thủ điều khoản Google.

### B. `NavigationNotificationManager`
- Khởi tạo Notification Channel với `NotificationManager.IMPORTANCE_HIGH` và `VISIBILITY_PUBLIC`.
- Sử dụng ID thông báo cố định (`NOTIFICATION_ID = 1001`) để chỉ duy trì 1 thông báo duy nhất.
- Hỗ trợ xuất bản thông báo song ngữ theo ngôn ngữ cấu hình qua `step.formatTitle(context)` và `step.formatContent(context)`.

### C. `SampleJourneySimulator`
- Cung cấp kịch bản 9 bước mẫu từ điểm xuất phát, qua các khúc rẽ, lối ra thứ 2 có tên đường đích, cập nhật cự ly, trạng thái chờ và hoàn thành hành trình.
- Cho phép người dùng kiểm tra kết nối với smartwatch trực tiếp trong nhà mà không cần ra đường thử nghiệm.

### D. Giao diện người dùng (Jetpack Compose Material 3)
- Hỗ trợ 3 chế độ giao diện: **Hệ thống (System Default)**, **Sáng (Light)**, **Tối (Dark)**.
- Hỗ trợ 3 lựa chọn ngôn ngữ: **Hệ thống (System Default)**, **Tiếng Việt**, **English**.
- Tự động lưu cấu hình qua `PreferencesManager`.
- Mỗi file giao diện tuân thủ giới hạn tối đa 200–250 dòng và hàm tối đa 30–50 dòng.

# Navi Noti

Navi Noti giúp người dùng nhận biết bước điều hướng tiếp theo trên đồng hồ mà không cần liên tục nhìn vào điện thoại. Khi đang đi theo bản đồ, ứng dụng hiển thị hướng rẽ và khoảng cách còn lại bằng một thông báo ngắn gọn.

## Trải nghiệm chính

Ví dụ thông báo:

> **Đường Nguyễn Huệ**  
> Còn 100 m · Rẽ trái

Tiêu đề ưu tiên tên đường sắp đi vào; dòng dưới cho biết còn bao xa và thao tác tiếp theo. Nội dung cần đọc nhanh trong một lần nhìn.

## Luồng sử dụng

1. Người dùng mở Navi Noti và bắt đầu sử dụng bản đồ để đi đến điểm cần đến.
2. Khi có hướng dẫn điều hướng, Navi Noti hiển thị bước tiếp theo trên điện thoại và đồng hồ.
3. Khi còn 100 m và 30 m đến chỗ rẽ, ứng dụng phát cảnh báo để người dùng kịp chuẩn bị.
4. Sau khi hoàn thành một bước, thông báo chuyển sang hướng dẫn kế tiếp.
5. Khi người dùng dừng hoặc kết thúc hành trình, thông báo điều hướng được gỡ bỏ.

## Nội dung thông báo

### Tiêu đề

Ưu tiên tên đường sắp đi vào. Nếu không có tên đường, dùng thao tác tiếp theo kèm mũi tên khi xác định được:

- Đường Nguyễn Huệ
- ← Rẽ trái
- → Rẽ phải
- ↑ Đi thẳng
- Đã đến nơi

### Nội dung

Cho biết khoảng cách và thao tác tiếp theo, không lặp lại tên đường đã có ở tiêu đề:

- Còn 100 m · Rẽ trái
- Còn 30 m · Rẽ phải
- Còn 150 m · Đi thẳng

Nếu không xác định được thao tác từ dữ liệu notification Google Maps, không tự suy đoán hướng rẽ hoặc hiển thị mũi tên thẳng; chỉ hiển thị khoảng cách.

Khi hành động đã xảy ra hoặc hành trình đã kết thúc, không tiếp tục hiển thị khoảng cách cũ. Nếu chưa có hướng dẫn kế tiếp, cần thể hiện trạng thái chờ thay vì để người dùng hiểu nhầm rằng thông tin cũ vẫn còn hiệu lực.

## Cách thông báo thay đổi

- Chỉ phát cảnh báo tại các mốc 100 m, 30 m, khi tới điểm rẽ (Maps chuyển maneuver hoặc sang trạng thái chờ hướng dẫn), và khi đến đích.
- Không phát cảnh báo định kỳ hoặc theo mỗi lần khoảng cách thay đổi.
- Khi tên đường thay đổi, cập nhật notification hiện tại bằng cùng notification ID một cách im lặng; không để tên đường cũ hiển thị và không phát âm thanh/rung chỉ vì đổi tên đường.
- Chỉ giữ một notification điều hướng hiện tại. Các lần cập nhật dùng cùng ID để thay nội dung, không tạo notification riêng mới.
- Notification được đánh dấu ongoing để hạn chế vuốt xóa. Nếu người dùng xóa khi điều hướng còn hoạt động, ứng dụng khôi phục notification im lặng.
- Không để lại thông báo cũ sau khi dừng hoặc hoàn thành hành trình.
- Nội dung trên đồng hồ cần ngắn gọn, không phụ thuộc vào việc người dùng mở điện thoại.

## Các trạng thái cần thể hiện

| Trạng thái | Trải nghiệm mong muốn |
|---|---|
| Chưa bắt đầu | Sẵn sàng để người dùng bắt đầu hành trình |
| Đang điều hướng | Hiển thị hướng dẫn kế tiếp và khoảng cách còn lại |
| Đang đến gần chỗ rẽ | Hiển thị tên đường cùng khoảng cách và hướng rẽ đã nhận diện |
| Đang chờ hướng dẫn | Không hiển thị nhầm hướng dẫn đã cũ |
| Tạm dừng | Cho biết hành trình đang tạm dừng và không gây hiểu nhầm rằng chỉ đường vẫn tiếp tục |
| Đã kết thúc | Gỡ thông báo điều hướng hiện tại |

## Nguyên tắc giao diện

- Xây dựng ứng dụng Android bằng Kotlin và Jetpack Compose, sử dụng các thành phần Material.
- Không tạo theme hoặc bảng màu riêng; ưu tiên màu hệ thống Android để giao diện hòa hợp với giao diện thiết bị.
- Tôn trọng chế độ sáng/tối của hệ thống và dùng màu động theo hệ thống trên các thiết bị hỗ trợ.
- Ưu tiên bố cục đơn giản, chữ rõ ràng và thao tác dễ hiểu.
- Màn hình chính tập trung vào trạng thái hành trình và thao tác bắt đầu/dừng.
- Không yêu cầu người dùng thao tác nhiều khi đang di chuyển.
- Thông báo trên đồng hồ là phần mở rộng của trải nghiệm điều hướng trên điện thoại; thông tin phải súc tích và nhất quán giữa hai thiết bị.

## Mục tiêu phiên bản đầu tiên

- Người dùng hiểu được hành trình đang hoạt động hay đã dừng.
- Thông báo cho biết đúng hướng đi kế tiếp và khoảng cách còn lại.
- Thông báo tự chuyển sang bước mới khi hướng dẫn thay đổi.
- Thông báo cũ được loại bỏ khi hành trình tạm dừng hoặc kết thúc.
- Người dùng có thể xem thử trải nghiệm với một hành trình mẫu trước khi sử dụng điều hướng thực tế.

## Trạng thái dự án

Ứng dụng đọc notification Google Maps, phân tích hướng dẫn và khoảng cách, hiển thị trạng thái trong app, đồng thời gửi một notification điều hướng ongoing có thể đồng bộ tới đồng hồ.

Hiện dự án mới có khung ứng dụng ban đầu. Trải nghiệm điều hướng, cập nhật thông báo và hiển thị trên đồng hồ vẫn cần được xây dựng.

# Navi Noti

Navi Noti giúp người dùng nhận biết bước điều hướng tiếp theo trên đồng hồ mà không cần liên tục nhìn vào điện thoại. Khi đang đi theo bản đồ, ứng dụng hiển thị hướng rẽ và khoảng cách còn lại bằng một thông báo ngắn gọn.

## Trải nghiệm chính

Ví dụ thông báo:

> **Rẽ trái**  
> Còn 250 m · Đường Nguyễn Huệ

Thông tin quan trọng nhất luôn được đưa lên trước: **sắp làm gì**, **rẽ vào đường nào** và **còn bao xa**. Nội dung cần đọc nhanh trong một lần nhìn, dễ hiểu khi đang đi bộ, đi xe đạp hoặc lái xe.

## Luồng sử dụng

1. Người dùng mở Navi Noti và bắt đầu sử dụng bản đồ để đi đến điểm cần đến.
2. Khi có hướng dẫn điều hướng, Navi Noti hiển thị bước tiếp theo trên điện thoại và đồng hồ.
3. Khi gần đến chỗ cần rẽ, khoảng cách trong thông báo được cập nhật để người dùng kịp chuẩn bị.
4. Sau khi hoàn thành một bước, thông báo chuyển sang hướng dẫn kế tiếp.
5. Khi người dùng dừng hoặc kết thúc hành trình, thông báo điều hướng được gỡ bỏ.

## Nội dung thông báo

### Tiêu đề

Nêu hành động tiếp theo bằng câu ngắn, quen thuộc:

- Rẽ trái
- Rẽ phải
- Đi thẳng
- Đi theo lối ra thứ 2
- Đã đến nơi

### Nội dung

Cho biết khoảng cách và tên đường sắp rẽ vào:

- Còn 250 m · Đường Nguyễn Huệ
- Còn 50 m · Đường Lê Lợi
- Đang đến điểm rẽ · Đường Trần Hưng Đạo

Nếu hướng dẫn không có tên đường, chỉ hiển thị khoảng cách và hành động; không để tên đường cũ xuất hiện cùng hướng dẫn mới.

Khi hành động đã xảy ra hoặc hành trình đã kết thúc, không tiếp tục hiển thị khoảng cách cũ. Nếu chưa có hướng dẫn kế tiếp, cần thể hiện trạng thái chờ thay vì để người dùng hiểu nhầm rằng thông tin cũ vẫn còn hiệu lực.

## Cách thông báo thay đổi

- Chỉ thông báo ở các thời điểm quan trọng: khi sắp đến chỗ rẽ (ví dụ còn khoảng 300 m), khi gần đến chỗ rẽ (ví dụ còn khoảng 100 m), khi hướng dẫn chuyển sang thao tác kế tiếp và khi đến đích.
- Các khoảng cách trên là mốc tham khảo; nên điều chỉnh theo tốc độ di chuyển và thời gian cần để người dùng chuẩn bị.
- Không gửi thông báo định kỳ theo thời gian hoặc mỗi khi khoảng cách thay đổi. Giữa các mốc quan trọng, có thể cập nhật khoảng cách trên thông báo hiện tại mà không làm đồng hồ rung hoặc phát thông báo mới.
- Chỉ giữ một thông báo điều hướng hiện tại; cập nhật thông báo đó khi sang mốc mới hoặc có hướng dẫn kế tiếp.
- Không để lại thông báo cũ sau khi dừng hoặc hoàn thành hành trình.
- Nội dung trên đồng hồ cần ngắn gọn, không phụ thuộc vào việc người dùng mở điện thoại.

## Các trạng thái cần thể hiện

| Trạng thái | Trải nghiệm mong muốn |
|---|---|
| Chưa bắt đầu | Sẵn sàng để người dùng bắt đầu hành trình |
| Đang điều hướng | Hiển thị hướng dẫn kế tiếp và khoảng cách còn lại |
| Đang đến gần chỗ rẽ | Làm rõ bước sắp thực hiện bằng thông tin ngắn, dễ nhận biết |
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

Hiện dự án mới có khung ứng dụng ban đầu. Trải nghiệm điều hướng, cập nhật thông báo và hiển thị trên đồng hồ vẫn cần được xây dựng.

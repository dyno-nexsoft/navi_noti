package com.dyno.navi_noti.data.model

import androidx.annotation.StringRes
import com.dyno.navi_noti.R

/// Trạng thái hoạt động của điều hướng theo tài liệu mô tả
enum class NavigationState(
    val displayName: String,
    @get:StringRes val titleRes: Int
) {
    IDLE("Chưa bắt đầu", R.string.status_idle),
    NAVIGATING("Đang điều hướng", R.string.status_navigating),
    APPROACHING("Đang đến gần chỗ rẽ", R.string.status_approaching),
    WAITING("Đang chờ hướng dẫn", R.string.status_waiting),
    PAUSED("Tạm dừng", R.string.status_paused),
    COMPLETED("Đã kết thúc", R.string.status_completed);

    /// Kiểm tra xem trạng thái hiện tại có đang hiển thị thông báo hay không
    val shouldShowNotification: Boolean
        get() = this == NAVIGATING || this == APPROACHING || this == WAITING
}

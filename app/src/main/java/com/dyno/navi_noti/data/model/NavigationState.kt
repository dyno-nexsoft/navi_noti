package com.dyno.navi_noti.data.model

import androidx.annotation.StringRes
import com.dyno.navi_noti.R

/// Trạng thái hoạt động của điều hướng theo tài liệu mô tả
enum class NavigationState(@get:StringRes val titleRes: Int) {
    IDLE(R.string.status_idle),
    NAVIGATING(R.string.status_navigating),
    APPROACHING(R.string.status_approaching),
    WAITING(R.string.status_waiting),
    PAUSED(R.string.status_paused),
    COMPLETED(R.string.status_completed);

    /// Kiểm tra xem trạng thái hiện tại có đang hiển thị thông báo hay không
    val shouldShowNotification: Boolean
        get() = this == NAVIGATING || this == APPROACHING || this == WAITING
}

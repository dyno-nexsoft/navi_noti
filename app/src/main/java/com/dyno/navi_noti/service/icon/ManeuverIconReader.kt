package com.dyno.navi_noti.service.icon

import android.app.Notification
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Icon
import android.os.Bundle
import android.util.Log
import com.dyno.navi_noti.data.model.ManeuverType

/// Đọc hướng rẽ từ các icon trong notification Google Maps.
/// Mũi tên nằm ở large icon (đã kiểm chứng với Maps trên Android 16) và progress tracker icon của Live Update.
/// Small icon là logo Maps cố định nên bỏ qua: học nó sẽ gán mọi bước thành một hướng.
class ManeuverIconReader(private val context: Context) {

    private val memory = ManeuverIconMemory(object : ManeuverIconMemory.Storage {
        private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        override fun load(): String? = prefs.getString(KEY_VOTES, null)
        override fun save(value: String) = prefs.edit().putString(KEY_VOTES, value).apply()
    })

    /// Icon mũi tên gốc của Maps (ưu tiên ảnh màu lớn) để chuyển tiếp sang notification của app
    var lastArrowBitmap: Bitmap? = null
        private set

    fun read(notification: Notification): IconReading {
        val icons = collectIcons(notification)
        lastArrowBitmap = icons.firstOrNull()?.bitmap
        val masks = icons.map { it.source to it.toMask() }
        return IconReading(masks)
    }

    /// Học từ notification có hướng rẽ ghi rõ bằng chữ
    fun learn(reading: IconReading, maneuver: ManeuverType) {
        reading.masks.forEach { (_, mask) -> memory.learn(mask.fingerprint(), maneuver) }
    }

    /// Ưu tiên icon đã ghi nhớ (chính xác), sau đó mới phân tích hình học.
    fun resolve(reading: IconReading): ManeuverType? {
        if (Log.isLoggable(TAG, Log.DEBUG)) logReading(reading)
        reading.masks.firstNotNullOfOrNull { (_, mask) -> memory.lookup(mask.fingerprint()) }
            ?.let { return it }
        return reading.masks
            .firstNotNullOfOrNull { (_, mask) -> ManeuverIconAnalyzer.classify(mask) }
    }

    private fun logReading(reading: IconReading) {
        reading.masks.forEach { (source, mask) ->
            val ascii = (0 until mask.height step 2).joinToString("\n") { y ->
                (0 until mask.width).joinToString("") { x -> if (mask[x, y]) "#" else "." }
            }
            Log.d(
                TAG,
                "icon $source memory=${memory.lookup(mask.fingerprint())} " +
                    "shape=${ManeuverIconAnalyzer.classify(mask)}\n$ascii"
            )
        }
    }

    private fun collectIcons(notification: Notification): List<SourceIcon> {
        val extras = notification.extras ?: Bundle.EMPTY
        return listOfNotNull(
            loadIcon(extras.iconOrBitmap(EXTRA_PROGRESS_TRACKER_ICON), IconSource.TRACKER),
            loadIcon(notification.getLargeIcon() ?: extras.iconOrBitmap(Notification.EXTRA_LARGE_ICON), IconSource.LARGE)
        )
    }

    @Suppress("DEPRECATION")
    private fun Bundle.iconOrBitmap(key: String): Icon? = when (val value = get(key)) {
        is Icon -> value
        is Bitmap -> Icon.createWithBitmap(value)
        else -> null
    }

    private fun loadIcon(icon: Icon?, source: IconSource): SourceIcon? {
        val drawable = runCatching { icon?.loadDrawable(context) }.getOrNull() ?: return null
        val size = BITMAP_SIZE
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        drawable.setBounds(0, 0, size, size)
        runCatching { drawable.draw(Canvas(bitmap)) }.getOrElse { return null }
        return SourceIcon(source, bitmap)
    }

    private class SourceIcon(val source: IconSource, val bitmap: Bitmap) {
        fun toMask(): IconMask {
            val scaled = Bitmap.createScaledBitmap(bitmap, MASK_SIZE, MASK_SIZE, true)
            val pixels = IntArray(MASK_SIZE * MASK_SIZE)
            scaled.getPixels(pixels, 0, MASK_SIZE, 0, 0, MASK_SIZE, MASK_SIZE)
            return IconMask.fromArgb(pixels, MASK_SIZE, MASK_SIZE)
        }
    }

    enum class IconSource { TRACKER, LARGE }

    class IconReading(val masks: List<Pair<IconSource, IconMask>>)

    private companion object {
        const val TAG = "NaviNoti"
        const val PREFS_NAME = "maneuver_icon_memory"
        const val KEY_VOTES = "votes"
        const val BITMAP_SIZE = 96
        const val MASK_SIZE = 48
        /// Notification.EXTRA_PROGRESS_TRACKER_ICON (API 36), dùng chuỗi để chạy được trên máy cũ
        const val EXTRA_PROGRESS_TRACKER_ICON = "android.progressTrackerIcon"
    }
}

package com.dyno.navi_noti.service.icon

/// Mặt nạ nhị phân của icon mũi tên (true = nét vẽ), tách khỏi Bitmap để phân tích/test trên JVM
class IconMask(val width: Int, val height: Int, private val bits: BooleanArray) {

    init {
        require(bits.size == width * height) { "Mask size mismatch" }
    }

    operator fun get(x: Int, y: Int): Boolean = bits[y * width + x]

    val filledCount: Int get() = bits.count { it }

    /// Vân tay 16x16 (64 ký tự hex) dùng để ghi nhớ icon đã từng thấy kèm hướng rõ ràng
    fun fingerprint(): String {
        val cells = BooleanArray(FINGERPRINT_SIZE * FINGERPRINT_SIZE)
        for (cy in 0 until FINGERPRINT_SIZE) {
            for (cx in 0 until FINGERPRINT_SIZE) {
                val x0 = cx * width / FINGERPRINT_SIZE
                val x1 = maxOf(x0 + 1, (cx + 1) * width / FINGERPRINT_SIZE)
                val y0 = cy * height / FINGERPRINT_SIZE
                val y1 = maxOf(y0 + 1, (cy + 1) * height / FINGERPRINT_SIZE)
                var filled = 0
                for (y in y0 until y1) for (x in x0 until x1) if (this[x, y]) filled++
                cells[cy * FINGERPRINT_SIZE + cx] = filled * 2 >= (x1 - x0) * (y1 - y0)
            }
        }
        return cells.toList().chunked(4).joinToString("") { nibble ->
            nibble.fold(0) { acc, bit -> (acc shl 1) or if (bit) 1 else 0 }.toString(16)
        }
    }

    companion object {
        private const val FINGERPRINT_SIZE = 16

        /// Tạo mask từ ASCII art ('#' = nét vẽ), tiện cho unit test
        fun fromRows(rows: List<String>): IconMask {
            val width = rows.maxOf(String::length)
            val bits = BooleanArray(width * rows.size)
            rows.forEachIndexed { y, row ->
                row.forEachIndexed { x, c -> bits[y * width + x] = c == '#' }
            }
            return IconMask(width, rows.size, bits)
        }

        /// Tạo mask từ pixel ARGB: ưu tiên kênh alpha (icon nền trong suốt),
        /// nếu icon có nền đặc thì so độ sáng với màu góc ảnh.
        fun fromArgb(pixels: IntArray, width: Int, height: Int): IconMask {
            val alpha = IntArray(pixels.size) { pixels[it] ushr 24 }
            val transparentCount = alpha.count { it < 128 }
            val bits = if (transparentCount > pixels.size / 10) {
                BooleanArray(pixels.size) { alpha[it] >= 128 }
            } else {
                val luma = IntArray(pixels.size) { luminance(pixels[it]) }
                val corners = listOf(0, width - 1, (height - 1) * width, height * width - 1)
                val background = corners.sumOf { luma[it] } / corners.size
                BooleanArray(pixels.size) { kotlin.math.abs(luma[it] - background) > 64 }
            }
            // Nét vẽ luôn chiếm ít diện tích hơn nền; nếu ngược lại thì đảo mask
            if (bits.count { it } * 10 > bits.size * 6) {
                for (i in bits.indices) bits[i] = !bits[i]
            }
            return IconMask(width, height, bits)
        }

        fun hammingDistance(a: String, b: String): Int {
            if (a.length != b.length) return Int.MAX_VALUE
            return a.indices.sumOf { Integer.bitCount(a[it].digitToInt(16) xor b[it].digitToInt(16)) }
        }

        private fun luminance(argb: Int): Int {
            val r = (argb shr 16) and 0xFF
            val g = (argb shr 8) and 0xFF
            val b = argb and 0xFF
            return (r * 299 + g * 587 + b * 114) / 1000
        }
    }
}

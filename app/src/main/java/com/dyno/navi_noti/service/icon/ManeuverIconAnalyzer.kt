package com.dyno.navi_noti.service.icon

import com.dyno.navi_noti.data.model.ManeuverType
import kotlin.math.abs
import kotlin.math.atan2

/// Nhận diện hướng rẽ từ hình dạng mũi tên của Google Maps.
/// Mũi tên Maps luôn có thân đi lên từ đáy icon; phần nét lệch khỏi thân cho biết hướng rẽ,
/// góc nghiêng của phần đó phân biệt rẽ thường / chếch / gắt / quay đầu.
object ManeuverIconAnalyzer {

    fun classify(mask: IconMask): ManeuverType? {
        val box = boundingBox(mask) ?: return null
        if (mask.filledCount * 50 < mask.width * mask.height) return null
        val outside = reachableBackground(mask)
        if (hasEnclosedHole(mask, box, outside)) {
            // Ghim điểm đến có chấm tròn nằm lọt trong lỗ; vòng xoay thì nét liền một khối
            return if (hasIsland(mask, outside)) ManeuverType.DESTINATION else ManeuverType.ROUNDABOUT
        }

        val stem = findStem(mask, box) ?: return null
        val left = mutableListOf<Pair<Int, Int>>()
        val right = mutableListOf<Pair<Int, Int>>()
        var total = 0
        for (y in box.top..box.bottom) for (x in box.left..box.right) {
            if (!mask[x, y]) continue
            total++
            when {
                x < stem.first - 1 -> left += x to y
                x > stem.last + 1 -> right += x to y
            }
        }

        val asymmetry = (left.size - right.size).toDouble() / total
        if (abs(asymmetry) < STRAIGHT_ASYMMETRY) {
            return if (left.isNotEmpty() && right.isNotEmpty()) ManeuverType.STRAIGHT else null
        }

        val isLeft = asymmetry > 0
        val side = if (isLeft) left else right
        val angle = principalAngle(side)
        // Ảnh có trục y hướng xuống: nhánh trái đi lên có góc dương, nhánh phải đi lên có góc âm
        val upAngle = if (isLeft) angle else -angle
        return when {
            abs(angle) >= VERTICAL_ANGLE -> ManeuverType.U_TURN
            upAngle >= DIAGONAL_ANGLE -> if (isLeft) ManeuverType.SLIGHT_LEFT else ManeuverType.SLIGHT_RIGHT
            upAngle <= -DIAGONAL_ANGLE -> if (isLeft) ManeuverType.SHARP_LEFT else ManeuverType.SHARP_RIGHT
            else -> if (isLeft) ManeuverType.TURN_LEFT else ManeuverType.TURN_RIGHT
        }
    }

    /// Thân mũi tên là đoạn nét chạm đáy icon; nếu có nhiều đoạn (quay đầu, rẽ gắt) chọn đoạn gần tâm nhất
    private fun findStem(mask: IconMask, box: Box): IntRange? {
        val runs = mutableListOf<IntRange>()
        var start = -1
        for (x in box.left..box.right + 1) {
            val filled = x <= box.right && mask[x, box.bottom]
            if (filled && start < 0) start = x
            if (!filled && start >= 0) {
                runs += start until x
                start = -1
            }
        }
        val center = mask.width / 2.0
        return runs.minByOrNull { abs((it.first + it.last) / 2.0 - center) }
    }

    /// Góc trục chính (độ) của tập điểm: 0 = nằm ngang, ±90 = thẳng đứng
    private fun principalAngle(points: List<Pair<Int, Int>>): Double {
        val mx = points.sumOf { it.first }.toDouble() / points.size
        val my = points.sumOf { it.second }.toDouble() / points.size
        var cxx = 0.0
        var cyy = 0.0
        var cxy = 0.0
        for ((x, y) in points) {
            cxx += (x - mx) * (x - mx)
            cyy += (y - my) * (y - my)
            cxy += (x - mx) * (y - my)
        }
        return Math.toDegrees(0.5 * atan2(2 * cxy, cxx - cyy))
    }

    /// Vòng xoay/ghim có lỗ khép kín: phần nền không tô loang tới được từ viền ảnh
    private fun hasEnclosedHole(mask: IconMask, box: Box, outside: BooleanArray): Boolean {
        var holes = 0
        for (y in box.top..box.bottom) for (x in box.left..box.right) {
            if (!mask[x, y] && !outside[y * mask.width + x]) holes++
        }
        return holes * 100 >= box.area * MIN_HOLE_PERCENT
    }

    /// Có khối nét nào không chạm vào nền bên ngoài (nằm lọt trong lỗ) hay không
    private fun hasIsland(mask: IconMask, outside: BooleanArray): Boolean {
        val w = mask.width
        val h = mask.height
        val seen = BooleanArray(w * h)
        for (start in 0 until w * h) {
            if (seen[start] || !mask[start % w, start / w]) continue
            var touchesOutside = false
            val queue = ArrayDeque(listOf(start))
            seen[start] = true
            while (queue.isNotEmpty()) {
                val i = queue.removeFirst()
                val x = i % w
                val y = i / w
                for ((nx, ny) in listOf(x + 1 to y, x - 1 to y, x to y + 1, x to y - 1)) {
                    if (nx !in 0 until w || ny !in 0 until h) { touchesOutside = true; continue }
                    val n = ny * w + nx
                    if (!mask[nx, ny]) {
                        if (outside[n]) touchesOutside = true
                    } else if (!seen[n]) {
                        seen[n] = true
                        queue += n
                    }
                }
            }
            if (!touchesOutside) return true
        }
        return false
    }

    /// Tô loang nền từ viền ảnh
    private fun reachableBackground(mask: IconMask): BooleanArray {
        val w = mask.width
        val h = mask.height
        val reached = BooleanArray(w * h)
        val queue = ArrayDeque<Int>()
        fun visit(x: Int, y: Int) {
            if (x !in 0 until w || y !in 0 until h) return
            val i = y * w + x
            if (reached[i] || mask[x, y]) return
            reached[i] = true
            queue += i
        }
        for (x in 0 until w) { visit(x, 0); visit(x, h - 1) }
        for (y in 0 until h) { visit(0, y); visit(w - 1, y) }
        while (queue.isNotEmpty()) {
            val i = queue.removeFirst()
            val x = i % w
            val y = i / w
            visit(x + 1, y); visit(x - 1, y); visit(x, y + 1); visit(x, y - 1)
        }
        return reached
    }

    private fun boundingBox(mask: IconMask): Box? {
        var left = Int.MAX_VALUE
        var top = Int.MAX_VALUE
        var right = -1
        var bottom = -1
        for (y in 0 until mask.height) for (x in 0 until mask.width) {
            if (!mask[x, y]) continue
            left = minOf(left, x); right = maxOf(right, x)
            top = minOf(top, y); bottom = maxOf(bottom, y)
        }
        return if (right < 0) null else Box(left, top, right, bottom)
    }

    private data class Box(val left: Int, val top: Int, val right: Int, val bottom: Int) {
        val area: Int get() = (right - left + 1) * (bottom - top + 1)
    }

    private const val STRAIGHT_ASYMMETRY = 0.12
    private const val DIAGONAL_ANGLE = 25.0
    private const val VERTICAL_ANGLE = 65.0
    private const val MIN_HOLE_PERCENT = 3
}

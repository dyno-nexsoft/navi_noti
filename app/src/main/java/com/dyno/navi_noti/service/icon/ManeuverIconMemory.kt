package com.dyno.navi_noti.service.icon

import com.dyno.navi_noti.data.model.ManeuverType

/// Ghi nhớ icon của Maps đã từng xuất hiện cùng hướng rẽ ghi rõ bằng chữ.
/// Lần sau Maps chỉ gửi icon (Live Update, text chỉ có khoảng cách/tên đường) vẫn tra ra được hướng.
class ManeuverIconMemory(private val storage: Storage) {

    interface Storage {
        fun load(): String?
        fun save(value: String)
    }

    private val votes = LinkedHashMap<String, MutableMap<ManeuverType, Int>>()

    init {
        storage.load()?.lineSequence()?.forEach(::decodeLine)
    }

    fun learn(fingerprint: String, maneuver: ManeuverType) {
        if (!maneuver.isTurn) return
        val entry = votes.remove(fingerprint) ?: mutableMapOf()
        entry[maneuver] = (entry[maneuver] ?: 0) + 1
        votes[fingerprint] = entry // đưa lên cuối để giữ các icon dùng gần đây
        while (votes.size > MAX_ENTRIES) votes.remove(votes.keys.first())
        storage.save(encode())
    }

    /// Trả về hướng chỉ khi các lần ghi nhận đủ áp đảo; icon dùng chung (ví dụ logo Maps) sẽ bị bỏ qua
    fun lookup(fingerprint: String): ManeuverType? {
        val tally = mutableMapOf<ManeuverType, Int>()
        for ((key, entry) in votes) {
            if (IconMask.hammingDistance(key, fingerprint) > MAX_HAMMING_DISTANCE) continue
            entry.forEach { (maneuver, count) -> tally[maneuver] = (tally[maneuver] ?: 0) + count }
        }
        val ranked = tally.entries.sortedByDescending { it.value }
        val best = ranked.firstOrNull() ?: return null
        val runnerUp = ranked.getOrNull(1)?.value ?: 0
        return best.key.takeIf { best.value >= runnerUp * 3 }
    }

    private fun encode(): String = votes.entries.joinToString("\n") { (key, entry) ->
        key + "=" + entry.entries.joinToString(",") { "${it.key.name}:${it.value}" }
    }

    private fun decodeLine(line: String) {
        val (key, rest) = line.split('=', limit = 2).takeIf { it.size == 2 } ?: return
        val entry = rest.split(',').mapNotNull { part ->
            val (name, count) = part.split(':').takeIf { it.size == 2 } ?: return@mapNotNull null
            val maneuver = ManeuverType.entries.firstOrNull { it.name == name } ?: return@mapNotNull null
            count.toIntOrNull()?.let { maneuver to it }
        }.toMap(mutableMapOf())
        if (entry.isNotEmpty()) votes[key] = entry
    }

    private companion object {
        const val MAX_ENTRIES = 200
        const val MAX_HAMMING_DISTANCE = 8
    }
}

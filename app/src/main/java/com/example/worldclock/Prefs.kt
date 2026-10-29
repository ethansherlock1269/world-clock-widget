package com.example.worldclock

import android.content.Context

data class City(val name: String, val tz: String)

object Prefs {
    // 想增加常用城市，直接在这里加一行即可（也可以在设置页选"自定义"输入任意时区ID）
    val CITIES = listOf(
        City("北京", "Asia/Shanghai"),
        City("台北", "Asia/Taipei"),
        City("纽约", "America/New_York"),
        City("东京", "Asia/Tokyo"),
        City("首尔", "Asia/Seoul"),
        City("新加坡", "Asia/Singapore"),
        City("曼谷", "Asia/Bangkok"),
        City("孟买", "Asia/Kolkata"),
        City("迪拜", "Asia/Dubai"),
        City("莫斯科", "Europe/Moscow"),
        City("伦敦", "Europe/London"),
        City("巴黎", "Europe/Paris"),
        City("柏林", "Europe/Berlin"),
        City("开罗", "Africa/Cairo"),
        City("约翰内斯堡", "Africa/Johannesburg"),
        City("悉尼", "Australia/Sydney"),
        City("奥克兰", "Pacific/Auckland"),
        City("洛杉矶", "America/Los_Angeles"),
        City("芝加哥", "America/Chicago"),
        City("多伦多", "America/Toronto"),
        City("圣保罗", "America/Sao_Paulo"),
        City("檀香山", "Pacific/Honolulu")
    )

    private fun sp(c: Context) = c.getSharedPreferences("clock_prefs", Context.MODE_PRIVATE)

    fun get(c: Context, id: Int): City {
        val p = sp(c)
        val tz = p.getString("tz_$id", null) ?: return CITIES[0]
        CITIES.firstOrNull { it.tz == tz }?.let { return it }
        val name = p.getString("name_$id", null) ?: tz.substringAfterLast('/').replace('_', ' ')
        return City(name, tz)
    }

    fun set(c: Context, id: Int, city: City) {
        sp(c).edit().putString("tz_$id", city.tz).putString("name_$id", city.name).apply()
    }

    fun remove(c: Context, id: Int) {
        sp(c).edit().remove("tz_$id").remove("name_$id").apply()
    }
}

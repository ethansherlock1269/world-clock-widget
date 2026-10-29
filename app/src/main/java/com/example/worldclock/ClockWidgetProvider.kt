package com.example.worldclock

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.widget.RemoteViews
import java.util.Calendar
import java.util.TimeZone

class ClockWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, mgr: AppWidgetManager, ids: IntArray) {
        ids.forEach { update(context, mgr, it) }
        scheduleNext(context)
    }

    override fun onEnabled(context: Context) = scheduleNext(context)

    override fun onDisabled(context: Context) {
        (context.getSystemService(Context.ALARM_SERVICE) as AlarmManager).cancel(tickIntent(context))
    }

    override fun onDeleted(context: Context, ids: IntArray) {
        ids.forEach { Prefs.remove(context, it) }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context, mgr: AppWidgetManager, id: Int, newOptions: Bundle?
    ) {
        update(context, mgr, id)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            ACTION_TICK, Intent.ACTION_TIME_CHANGED, Intent.ACTION_TIMEZONE_CHANGED -> {
                updateAll(context)
                scheduleNext(context)
            }
        }
    }

    companion object {
        const val ACTION_TICK = "com.example.worldclock.TICK"

        fun updateAll(ctx: Context) {
            val mgr = AppWidgetManager.getInstance(ctx)
            mgr.getAppWidgetIds(ComponentName(ctx, ClockWidgetProvider::class.java))
                .forEach { update(ctx, mgr, it) }
        }

        fun update(ctx: Context, mgr: AppWidgetManager, id: Int) {
            val rv = RemoteViews(ctx.packageName, R.layout.widget_clock)
            val city = Prefs.get(ctx, id)
            val cal = Calendar.getInstance(TimeZone.getTimeZone(city.tz))
                .apply { timeInMillis = System.currentTimeMillis() }
            val h = cal.get(Calendar.HOUR)        // 0..11
            val m = cal.get(Calendar.MINUTE)
            rv.setInt(R.id.hour_hand, "setImageLevel", ((h + m / 60f) / 12f * 10000f).toInt())
            rv.setInt(R.id.minute_hand, "setImageLevel", (m / 60f * 10000f).toInt())

            // 时钟图层自动等比缩放；城市名的字号/位置按挂件实际大小设置
            val o = mgr.getAppWidgetOptions(id)
            val land = ctx.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
            val wDp = (if (land) o.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH)
                       else o.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)).let { if (it > 0) it else 110 }
            val hDp = (if (land) o.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT)
                       else o.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT)).let { if (it > 0) it else 110 }
            val s = minOf(wDp / 198f, hDp / 264f)      // 每个设计单位 = s dp
            val density = ctx.resources.displayMetrics.density
            rv.setTextViewText(R.id.city_name, city.name)
            rv.setTextViewTextSize(R.id.city_name, TypedValue.COMPLEX_UNIT_DIP, maxOf(8f, 14.8f * s))
            // 文字中心应在表盘中心上方 20 单位 = 画布中心上方 41 单位：底部内边距 = 2 x 41 x s
            rv.setViewPadding(R.id.city_name, 0, 0, 0, (82f * s * density).toInt())

            // 点击挂件 -> 打开时区设置
            val intent = Intent(ctx, ConfigActivity::class.java)
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
            intent.data = Uri.parse("worldclock://config/$id")
            val pi = PendingIntent.getActivity(
                ctx, id, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            rv.setOnClickPendingIntent(R.id.root, pi)
            mgr.updateAppWidget(id, rv)
        }

        private fun tickIntent(ctx: Context): PendingIntent {
            val i = Intent(ctx, ClockWidgetProvider::class.java).setAction(ACTION_TICK)
            return PendingIntent.getBroadcast(
                ctx, 0, i, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        fun scheduleNext(ctx: Context) {
            val am = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val next = (System.currentTimeMillis() / 60000 + 1) * 60000 + 300
            val pi = tickIntent(ctx)
            val exactOk = Build.VERSION.SDK_INT < 31 || am.canScheduleExactAlarms()
            if (exactOk) am.setExact(AlarmManager.RTC, next, pi) else am.set(AlarmManager.RTC, next, pi)
        }
    }
}

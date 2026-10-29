package com.example.worldclock

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import java.util.TimeZone

class ConfigActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(RESULT_CANCELED)

        val id = intent.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID
        )
        if (id == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        val dp = resources.displayMetrics.density
        val pad = (20 * dp).toInt()
        val names = Prefs.CITIES.map { it.name } + "自定义时区ID…"
        val customPos = names.size - 1

        val spinner = Spinner(this).apply {
            adapter = ArrayAdapter(
                this@ConfigActivity, android.R.layout.simple_spinner_dropdown_item, names
            )
        }
        val input = EditText(this).apply {
            hint = "例如 Europe/Rome、America/Denver"
            setSingleLine()
            visibility = View.GONE
        }

        val cur = Prefs.get(this, id)
        val idx = Prefs.CITIES.indexOfFirst { it.tz == cur.tz }
        if (idx >= 0) {
            spinner.setSelection(idx)
        } else {
            spinner.setSelection(customPos)
            input.setText(cur.tz)
            input.visibility = View.VISIBLE
        }
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>?, v: View?, pos: Int, rowId: Long) {
                input.visibility = if (pos == customPos) View.VISIBLE else View.GONE
            }

            override fun onNothingSelected(p: AdapterView<*>?) {}
        }

        val save = Button(this).apply {
            text = "保存"
            setOnClickListener {
                val pos = spinner.selectedItemPosition
                val city: City
                if (pos < Prefs.CITIES.size) {
                    city = Prefs.CITIES[pos]
                } else {
                    val tzId = input.text.toString().trim()
                    if (!TimeZone.getAvailableIDs().contains(tzId)) {
                        Toast.makeText(this@ConfigActivity, "时区ID无效，请检查拼写", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                    city = City(tzId.substringAfterLast('/').replace('_', ' '), tzId)
                }
                Prefs.set(this@ConfigActivity, id, city)
                ClockWidgetProvider.update(
                    this@ConfigActivity, AppWidgetManager.getInstance(this@ConfigActivity), id
                )
                ClockWidgetProvider.scheduleNext(this@ConfigActivity)
                setResult(RESULT_OK, Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id))
                finish()
            }
        }

        setContentView(LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(pad, pad, pad, pad)
            addView(TextView(this@ConfigActivity).apply { text = "选择时区"; textSize = 18f })
            addView(spinner)
            addView(input)
            addView(save)
        })
    }
}

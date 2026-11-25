package com.example.eggmacro

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        val layout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(28, 36, 28, 24); gravity = Gravity.CENTER_VERTICAL }
        layout.addView(TextView(this).apply { text = "낚시 터치 보조 (간단 버전)\n\n화면 좌표를 지정해 일정 간격으로 터치합니다. 낚시 화면을 자동 인식하지는 않습니다."; textSize = 18f })
        fun field(label: String, key: String, default: String): EditText {
            layout.addView(TextView(this).apply { text = label; textSize = 15f })
            return EditText(this).apply { inputType = 2; setText(prefs.getString(key, default)); layout.addView(this, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT) }
        }
        val x = field("터치 X 좌표 (픽셀)", "x", "540")
        val y = field("터치 Y 좌표 (픽셀)", "y", "1400")
        val interval = field("터치 간격 (밀리초, 최소 500)", "interval", "1200")
        layout.addView(Button(this).apply { text = "접근성 권한 설정 열기"; setOnClickListener { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) } })
        layout.addView(Button(this).apply { text = "화면 위 시작/정지 버튼 표시"; setOnClickListener {
            prefs.edit().putInt("x", x.text.toString().toIntOrNull() ?: 540).putInt("y", y.text.toString().toIntOrNull() ?: 1400).putLong("interval", (interval.text.toString().toLongOrNull() ?: 1200L).coerceAtLeast(500L)).apply()
            if (!Settings.canDrawOverlays(this@MainActivity)) startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
            else startService(Intent(this@MainActivity, TapService::class.java))
        } })
        layout.addView(TextView(this).apply { text = "주의: 접근성/화면 오버레이 권한이 필요합니다. 게임 규칙에 따라 매크로 사용이 제한될 수 있으며 계정 제재 위험이 있습니다. 언제든 중지 버튼을 누르세요."; textSize = 13f })
        setContentView(ScrollView(this).apply { addView(layout) })
    }
}

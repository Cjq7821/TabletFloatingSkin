package com.cjq7821.poweramp.v3.skins.tabletfloating

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.maxmpz.poweramp.player.PowerampAPIHelper.getPowerampPackageName

/** 获取已安装的 Poweramp 包名；未安装时提示并返回 null */
internal fun Context.getPaPak(): String? {
	val pak = getPowerampPackageName(this, false)
	if(pak == null) {
		Toast.makeText(this, R.string.skin_poweramp_not_installed, Toast.LENGTH_LONG).show()
	}
	return pak
}

/**
 * 皮肤信息/应用入口界面。
 * 提供"直接应用皮肤"和"打开 Poweramp 皮肤设置"两个操作。
 */
class SkinInfoActivity : Activity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_skin_info)
	}

	/** 启动 Poweramp 并直接应用本皮肤 */
	fun startWithSkin(view: View?) {
		val pak = getPaPak() ?: return

		val intent = Intent(Intent.ACTION_MAIN)
			.setClassName(pak, "com.maxmpz.audioplayer.StartupActivity")
			.putExtra("theme_pak", packageName)
			.putExtra("theme_id", R.style.TabletFloatingSkinDark)
		startActivity(intent)
		finish()
	}

	/** 打开 Poweramp 的皮肤设置页并定位到本皮肤 */
	fun openPowerampThemeSettings(view: View?) {
		val pak = getPaPak() ?: return

		val intent = Intent(Intent.ACTION_MAIN)
			.setClassName(pak, "com.maxmpz.audioplayer.SettingsActivity")
			.putExtra("open", "theme")
			.putExtra("theme_pak", packageName)
			.putExtra("theme_id", R.style.TabletFloatingSkinDark)
		// open=theme 需要 startActivityForResult
		@Suppress("DEPRECATION")
		startActivityForResult(intent, 1)
		finish()
	}
}

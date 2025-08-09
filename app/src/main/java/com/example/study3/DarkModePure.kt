package com.example.study3

import android.app.WallpaperManager
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

//此文件已经过测稳定性 !!Stabled
class DarkModePure : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dark_pure)

        if (intent.getBooleanExtra("FROM_TILE", false)) {
            fromTile()
            lifecycleScope.launch {
                delay(1000)
                finish()
            }
        }

        if (intent.getBooleanExtra("FROM_HOME",false)){
            lifecycleScope.launch { delay(500) }

            fromHome()
            lifecycleScope.launch {
                delay(1000)
                finish()
            }
        }

    }
    //onCreate END


    //Functions
    private fun setDark() {
        val wallpaperManager = WallpaperManager.getInstance(this)
        val bitmap =
            BitmapFactory.decodeFile("$filesDir/images/selected_image_dark.jpg")
        wallpaperManager.setBitmap(bitmap, null, false,
            WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK)
    }

    private fun setLight() {
        val wallpaperManager = WallpaperManager.getInstance(this)
        val bitmap = BitmapFactory.decodeFile("$filesDir/images/selected_image_light.jpg")
        wallpaperManager.setBitmap(bitmap, null, false,
            WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK)
    }

    private fun fromHome(){
        val sharedPreferences=getSharedPreferences("app_prefs", MODE_PRIVATE)
        if(!sharedPreferences.contains("is_dark_wallpaper_set?") || !sharedPreferences.contains("is_light_wallpaper_set?")){
            Toast.makeText(this, "您没有设置完全部两张壁纸", Toast.LENGTH_SHORT).show()
            return
        }

        val nightModeFlags = resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK
        when (nightModeFlags) {
            android.content.res.Configuration.UI_MODE_NIGHT_YES -> {
                setDark()
            }
            android.content.res.Configuration.UI_MODE_NIGHT_NO -> {
                setLight()
            }
        }
    }

    private fun fromTile() {
        var sharedPreferences=getSharedPreferences("app_prefs", MODE_PRIVATE)
        if(!sharedPreferences.contains("is_dark_wallpaper_set?") || !sharedPreferences.contains("is_light_wallpaper_set?")){
            Toast.makeText(this, "您没有设置完全部两张壁纸", Toast.LENGTH_SHORT).show()
            return
        }

        /*
        val fileDir = File(filesDir, "images")
        val img1File = File(fileDir, "selected_image_light.jpg") // 第一张图片
        val img2File = File(fileDir, "selected_image_dark.jpg") // 第二张图片

        if (img1File.exists() && img2File.exists()) {
            //Toast.makeText(this, "文件存在", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "您没有设置完全部两张壁纸", Toast.LENGTH_SHORT).show()
            return
        }*/

        sharedPreferences = getSharedPreferences("tile_prefs", MODE_PRIVATE)
        val isEnabled = sharedPreferences.getBoolean("isEnabled", true)
        if (isEnabled) {
            setDark()
        } else {
            setLight()
        }
    }


}
//class END









package com.example.study3

import android.annotation.SuppressLint
import android.app.Dialog
import android.app.WallpaperManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.SwitchCompat
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.ui.platform.ComposeView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import androidx.core.content.edit
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.core.graphics.scale
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.DelicateCoroutinesApi
import androidx.compose.animation.slideInVertically
import androidx.compose.material.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue


class DarkMode: AppCompatActivity() {
    //数值设置区
    private lateinit var Switch1: SwitchCompat
    private lateinit var Switch2: SwitchCompat
    private val COOLDOWN_TIME_2 = 4000L
    private var lastClickTime: Long = 0
    companion object { const val PERMISSION_REQUEST_CODE = 1 }




    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dark_mode)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.darkmode)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }



        //准备工作
        preCheck()




        //toolbar按钮
        //按钮：toolbar返回上级
        val buttonT1 = findViewById<ImageButton>(R.id.buttonToolbarExit)
        buttonT1.setOnClickListener {
            finish() }
        //按钮：toolbar帮助



        //开关：将选择的壁纸保存到外部
        Switch1 = findViewById(R.id.switchToGallery)
        Switch1.setOnCheckedChangeListener { _, isChecked ->
            saveSwitchState("saveToPublic", isChecked)
        }
        restoreSwitchState("saveToPublic")
        //开关：壁纸slightMove
        Switch2 = findViewById(R.id.switchSlightMove)
        Switch2.setOnCheckedChangeListener { _, isChecked ->
            saveSwitchState("slightmove", isChecked)
        }
        restoreSwitchState("slightmove")

        //主区域按钮
        //按钮：选择/更改深色壁纸
        val buttonM1 = findViewById<Button>(R.id.buttonChangeDark)
        buttonM1.setOnClickListener {
            val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            sharedPreferences.edit {
                putString("whatmode?", "dark")
                apply()
            }
            openGallery()
        }
        //按钮：选择/更改浅色壁纸
        val buttonM2 = findViewById<Button>(R.id.buttonChangeLight)
        buttonM2.setOnClickListener {
            val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            sharedPreferences.edit {
                putString("whatmode?", "light")
                apply()
            }
            openGallery()
        }
        //按钮：返回桌面不finish
        val buttonM3 = findViewById<Button>(R.id.buttonSuperExit)
        buttonM3.setOnClickListener {
            val intent = Intent(Intent.ACTION_MAIN)
            intent.addCategory(Intent.CATEGORY_HOME)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }
        //按钮：切换深色
        val buttonM4 = findViewById<Button>(R.id.buttonSwitchDark)
        buttonM4.setOnClickListener {
            SwitchNow("dark")
        }
        //按钮：切换浅色
        val buttonM5 = findViewById<Button>(R.id.buttonSwitchLight)
        buttonM5.setOnClickListener {
            SwitchNow("light")

        }
        //按钮：添加快捷方式
        val buttonM6 = findViewById<Button>(R.id.buttonAddTile)
        buttonM6.setOnClickListener {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastClickTime < COOLDOWN_TIME_2) {
                return@setOnClickListener
            } else {
                lastClickTime = currentTime
                Toast.makeText(
                    this,
                    "请确保开启了创建快捷方式权限丨您也可使用磁贴",
                    Toast.LENGTH_LONG
                ).show()
            }
            createShortcut()

        }
        //按钮：清除壁纸
        val buttonM7 = findViewById<Button>(R.id.buttonClear)
        buttonM7.setOnClickListener {
            ClearWallPaper()
            Toast.makeText(this, "已清除", Toast.LENGTH_SHORT).show()
        }
        //按钮：设置微动值
        val buttonM8=findViewById<Button>(R.id.buttonSetValue)
        buttonM8.setOnClickListener() {
            val dialog = Dialog(this)
            val dialogView = LayoutInflater.from(this).inflate(R.layout.activity_dark_mode_settings, null)
            dialog.setContentView(dialogView)
            val title: TextView = dialogView.findViewById(R.id.dialog_title)
            val title2:TextView = dialogView.findViewById(R.id.dialog_description)
            val input: EditText = dialogView.findViewById(R.id.dialog_input)
            val button: Button = dialogView.findViewById(R.id.dialog_button)



            button.setOnClickListener {
                val userInput = input.text.toString()
                setValue(userInput)
                dialog.dismiss()
            }
            dialog.show()

        }

    }



    //Private Functions
    private fun saveSwitchState(key: String, isChecked: Boolean) {
        val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        sharedPreferences.edit {
            putBoolean(key, isChecked)
            apply()
        }
    }

    private fun restoreSwitchState(key: String) {
        val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val isChecked = sharedPreferences.getBoolean(key, false)
        if (key == "saveToPublic") {
            Switch1.isChecked = isChecked
        }
        if (key == "slightmove") {
            Switch2.isChecked = isChecked
        }
    }

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                val selectedImageUri = result.data?.data
                selectedImageUri?.let { saveImage(it) }
            }
        }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageLauncher.launch(intent)
    }

    private fun saveImage(uri: Uri) {

        fun cropBitmap(bitmapScaled: Bitmap, x: Int, y: Int, width: Int, height: Int): Bitmap {
            return Bitmap.createBitmap(bitmapScaled, x, y, width, height)
        }

        fun Info(bitmapForScale: Bitmap): Bitmap {
            //获取图片分辨率
            val picWidth = bitmapForScale.width
            val picHeight = bitmapForScale.height
            //获取屏幕分辨率
            val displayMetrics = DisplayMetrics()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val display = windowManager.currentWindowMetrics.bounds
                displayMetrics.widthPixels = display.width()
                displayMetrics.heightPixels = display.height()
            } else {
                @Suppress("DEPRECATION")
                windowManager.defaultDisplay.getMetrics(displayMetrics)
            }
            val screenWidth = displayMetrics.widthPixels
            val screenHeight = displayMetrics.heightPixels

            //缩放：可以把任何图片缩放并裁剪为手机分辨率（似乎可以优化？）
            var bitmapScaled: Bitmap
            val heightRatio = screenHeight.toFloat() / picHeight.toFloat()
            val newWidth = (picWidth * heightRatio).toInt()
            val newHeight = (picHeight * heightRatio).toInt()
            bitmapScaled = bitmapForScale.scale(newWidth, newHeight)

            val picWidth2= bitmapScaled.width

            if (picWidth2>=screenWidth){
                var x=0
                x=(picWidth2/2).toInt()-(screenWidth/2).toInt()
                val y=0
                val width=screenWidth
                val height=screenHeight
                //Toast.makeText(this,"$x,$y,$width,$height",Toast.LENGTH_SHORT).show()
                bitmapScaled=cropBitmap(bitmapScaled,x,y,width,height)
            }
            if(picWidth2<screenWidth){
                val widthRatio = screenWidth.toFloat() / picWidth.toFloat()
                val newWidth = (picWidth * widthRatio).toInt()
                val newHeight = (picHeight * widthRatio).toInt()
                bitmapScaled = bitmapForScale.scale(newWidth, newHeight)

                val picHeight2= bitmapScaled.height
                val x=0
                var y=0
                y=(picHeight2/2).toInt()-(screenHeight/2).toInt()
                val width=screenWidth
                val height=screenHeight
                //Toast.makeText(this,"$x,$y,$width,$height",Toast.LENGTH_SHORT).show()
                bitmapScaled=cropBitmap(bitmapScaled,x,y,width,height)
            }

            //微动
            val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            if(Switch2.isChecked) {
                if(sharedPreferences.contains("SlightValue")){
                    val number = 0
                    val x=0
                    val y=sharedPreferences.getInt("SlightValue",number)
                    val width=screenWidth
                    val height=screenHeight-2*y
                    bitmapScaled=cropBitmap(bitmapScaled,x,y,width,height)
                }
                else
                {
                    val x=0
                    val y=50
                    val width=screenWidth
                    val height=screenHeight-100
                    bitmapScaled=cropBitmap(bitmapScaled,x,y,width,height)
                }
            }

            return bitmapScaled

        }

        contentResolver.openInputStream(uri)?.use { inputStream ->
            val bitmap = BitmapFactory.decodeStream(inputStream)
            val bitmapForScale = bitmap.copy(Bitmap.Config.ARGB_8888, true)

            val croppedBitmap = Info(bitmapForScale)


            val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            val whatMode = sharedPreferences.getString("whatmode?", "")
            val fileName = when (whatMode) {
                "dark" -> "selected_image_dark.jpg"
                "light" -> "selected_image_light.jpg"
                else -> "selected_image_default.jpg"
            }
            val fileDir = File(filesDir, "images")
            if (!fileDir.exists()) {
                fileDir.mkdirs()
            }
            val file = File(fileDir, fileName)
            FileOutputStream(file).use { outputStream ->
                croppedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            }

            if (Switch1.isChecked) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/使用过的壁纸")
                }
                val imageUri = contentResolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    contentValues
                )
                imageUri?.let {
                    val outputStream: OutputStream? = contentResolver.openOutputStream(it)
                    outputStream?.use { stream ->
                        croppedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                    }
                }
            }


            //写入参数：是否已设置
            when (whatMode) {
                "dark" -> {
                    sharedPreferences.edit {
                        putBoolean("is_dark_wallpaper_set?", true)
                        apply()
                    }
                    loadImage()
                }

                "light" -> {
                    sharedPreferences.edit {
                        putBoolean("is_light_wallpaper_set?", true)
                        apply()
                    }
                    loadImage()
                }
            }


        }
    }

    private fun loadImage() {
        val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        if (sharedPreferences.contains("is_dark_wallpaper_set?") && (!sharedPreferences.contains("Clear"))) {
            val fileName = "selected_image_dark.jpg"
            val fileDir = File(filesDir, "images")
            val file = File(fileDir, fileName)

            if (file.exists()) {
                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                val imageView = findViewById<ImageView>(R.id.imageDark)
                imageView.setImageBitmap(bitmap)
            }

        }
        if (sharedPreferences.contains("is_light_wallpaper_set?") && (!sharedPreferences.contains("Clear"))) {
            val fileName = "selected_image_light.jpg"
            val fileDir = File(filesDir, "images")
            val file = File(fileDir, fileName)

            if (file.exists()) {
                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                val imageView = findViewById<ImageView>(R.id.imageLight)
                imageView.setImageBitmap(bitmap)
            }
        }
        if (sharedPreferences.contains("Clear")) {
            val imageView1 = findViewById<ImageView>(R.id.imageLight)
            val bitmap1 = BitmapFactory.decodeResource(resources, R.drawable.ic_notset)
            imageView1.setImageBitmap(bitmap1)

            val imageView2 = findViewById<ImageView>(R.id.imageDark)
            val bitmap2 = BitmapFactory.decodeResource(resources, R.drawable.ic_notset)
            imageView2.setImageBitmap(bitmap2)
            sharedPreferences.edit {
                remove("Clear")
                apply()
            }
        }

    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun preCheck() {
        //准备工作1：申请储存
        //无需申请此权限，使用媒体库api来保存图片到外部即可！

        //准备工作2：检查壁纸是否设置，动态切换样式
        val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        if (sharedPreferences.contains("is_dark_wallpaper_set?")) {
            loadImage()
        }

        if (sharedPreferences.contains("is_light_mode_set?")) {
            loadImage()
        }

        if (!sharedPreferences.contains("whatmode?")) {
            sharedPreferences.edit {
                putString("whatmode?", "")
                apply()
            }
        }
        //后级工作：动态切换文字
        if (!sharedPreferences.contains("is_dark_wallpaper_set?")) {
            val text001 = findViewById<TextView>(R.id.text001)
            text001.setText("选择一张图片作为深色模式壁纸")
            val buttonChangeDark = findViewById<Button>(R.id.buttonChangeDark)
            buttonChangeDark.setText("选择")

        }
        if (!sharedPreferences.contains("is_light_wallpaper_set?")) {
            val text002 = findViewById<TextView>(R.id.text002)
            text002.setText("选择一张图片作为浅色模式壁纸")
            val buttonChangeLight = findViewById<Button>(R.id.buttonChangeLight)
            buttonChangeLight.setText("选择")
        }
        //Description
        val composeView = findViewById<ComposeView>(R.id.compose1)
        composeView.setContent {
            var isVisible by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                isVisible = true
            }
            AnimatedVisibility(
                visible = isVisible,
                enter = slideInVertically(
                    initialOffsetY = { 300 },
                    animationSpec = tween(durationMillis = 300)
                )
            ){
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(13.dp)
                        .background(colorResource(id = R.color.HeadBackground))
                        .border(
                            2.dp,
                            colorResource(id = R.color.HeadText),
                            RoundedCornerShape(20.dp)
                        )
                        .padding(16.dp)

                ) {
                    Text(
                        text = getString(R.string.description_darkmode),
                        style = TextStyle(fontSize = 14.sp),
                        color = colorResource(id = R.color.HeadText),
                        )
                    }
                }

        }


    }

    private fun ClearWallPaper() {
        val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        sharedPreferences.edit {
            remove("is_dark_wallpaper_set?")
            remove("is_light_wallpaper_set?")
            putString("Clear", "")
            apply()
            loadImage()
        }
    }

    private fun SwitchNow(mode: String) {
        if (mode == "dark") {
            findViewById<Button>(R.id.buttonSwitchDark).setText("请等待应用自行退出，避免卡顿")
            findViewById<Button>(R.id.buttonSwitchDark).setBackgroundColor(resources.getColor(R.color.ButtonBgHighlight))
            val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            if (sharedPreferences.contains("running")) {
                runOnUiThread {
                    findViewById<Button>(R.id.buttonSwitchDark).setText("请勿重复点击,否则易导致界面卡死!")
                    findViewById<Button>(R.id.buttonSwitchDark).setBackgroundColor(resources.getColor(R.color.ButtonBgVeryHeavy))
                }
                return
            }
            GlobalScope.launch {
                sharedPreferences.edit {
                    putString("running", "1")
                    apply()
                }
                if (sharedPreferences.contains("is_dark_wallpaper_set?")) {
                    val fileName = "selected_image_dark.jpg"
                    val fileDir = File(filesDir, "images")
                    val file = File(fileDir, fileName)
                    if (file.exists()) {
                        val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                        val wallpaperManager = WallpaperManager.getInstance(this@DarkMode)
                        wallpaperManager.setBitmap(
                            bitmap, null, false,
                            WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK
                        )
                    }
                    lifecycleScope.launch { delay(500) }
                    val intent = Intent(Intent.ACTION_MAIN)
                    intent.addCategory(Intent.CATEGORY_HOME)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    sharedPreferences.edit {
                        remove("running")
                        apply()
                    }
                    runOnUiThread {
                        finish()
                    }

                } else {
                    runOnUiThread {
                        findViewById<Button>(R.id.buttonSwitchDark).setText("您未设置深色模式壁纸")
                    }
                    sharedPreferences.edit {
                        remove("running")
                        apply()
                    }
                }
            }

        }
        if (mode == "light") {
            findViewById<Button>(R.id.buttonSwitchLight).setText("请等待应用自行退出，避免卡顿")
            findViewById<Button>(R.id.buttonSwitchLight).setBackgroundColor(resources.getColor(R.color.ButtonBgHighlight))

            val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            if (sharedPreferences.contains("running")) {
                runOnUiThread {
                    findViewById<Button>(R.id.buttonSwitchLight).setText("请勿重复点击,否则易导致界面卡死!")
                    findViewById<Button>(R.id.buttonSwitchLight).setBackgroundColor(resources.getColor(R.color.ButtonBgVeryHeavy))
                }
                return
            }

            GlobalScope.launch(Dispatchers.IO) {
                sharedPreferences.edit {
                    putString("running", "1")
                    apply()
                }
                val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                if (sharedPreferences.contains("is_light_wallpaper_set?")) {
                    val fileName = "selected_image_light.jpg"
                    val fileDir = File(filesDir, "images")
                    val file = File(fileDir, fileName)
                    if (file.exists()) {
                        val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                        val wallpaperManager = WallpaperManager.getInstance(this@DarkMode)
                        wallpaperManager.setBitmap(
                            bitmap, null, false,
                            WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK
                        )
                    }
                    lifecycleScope.launch { delay(500) }
                    val intent = Intent(Intent.ACTION_MAIN)
                    intent.addCategory(Intent.CATEGORY_HOME)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    sharedPreferences.edit {
                        remove("running")
                        apply()
                    }
                    runOnUiThread {
                        finish()
                    }

                } else {
                    runOnUiThread {
                        findViewById<Button>(R.id.buttonSwitchLight).setText("您未设置浅色模式壁纸")
                    }
                    sharedPreferences.edit {
                        remove("running")
                        apply()
                    }
                }
            }
        }
    }

    private fun setValue(content:String){
        if (content.isEmpty()){
            Toast.makeText(this, "填点东西，哥们", Toast.LENGTH_SHORT).show()
            return
        }
        var number = content.toInt()
        if(number==114514){
            Toast.makeText(this, "禁止输入一个一个一个恶臭代码", Toast.LENGTH_SHORT).show()
            return
        }
        if(number<=200){
            val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            sharedPreferences.edit{
                putInt("SlightValue",number)
            }
            Toast.makeText(this, "已设置为$number", Toast.LENGTH_SHORT).show()
            return
        }
        else{
            Toast.makeText(this, "数值过大", Toast.LENGTH_SHORT).show()
            return
        }
    }

    private fun createShortcut(){
        val shortcutManager = getSystemService(ShortcutManager::class.java)

        val shortcutIntent = Intent(this, DarkModePure::class.java).apply {
            action = Intent.ACTION_MAIN
            putExtra("FROM_HOME",true)
        }
        val shortcut = ShortcutInfo.Builder(this, "dark_mode_shortcut")
            .setShortLabel("切换壁纸")
            .setLongLabel("根据当前模式切换壁纸")
            .setIcon(Icon.createWithResource(this, R.drawable.ic_shortcut))
            .setIntent(shortcutIntent)
            .build()

        shortcutManager?.requestPinShortcut(shortcut, null)
    }




}





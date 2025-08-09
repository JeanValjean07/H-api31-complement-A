package com.example.study3

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }



        //按钮：Toolbar退出（自杀）
        val buttonT1 = findViewById<Button>(R.id.buttonExit)
        buttonT1.setOnClickListener {
            val intent = Intent(Intent.ACTION_MAIN)
            intent.addCategory(Intent.CATEGORY_HOME)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            lifecycleScope.launch {
                delay(500)
            }
            val pid = android.os.Process.myPid()
            android.os.Process.killProcess(pid)
            System.exit(0)
        }
        //按钮：Toolbar帮助


        //按钮：Toolbar指南




        //按钮：深色模式壁纸
        val button6 = findViewById<Button>(R.id.buttonSwitchDark)
        button6.setOnClickListener {
            val intent = Intent(this, DarkMode::class.java)
            startActivity(intent)
        }
        //按钮：通知管理
        val button7=findViewById<Button>(R.id.buttonNotiController)
        button7.setOnClickListener {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                Toast.makeText(this,"理论上,您的设备无需使用此功能",Toast.LENGTH_SHORT).show()
            }
            val intent = Intent(this, NotiControl::class.java)
            startActivity(intent)
        }





    }
    //onCreate END



    //Functions



}
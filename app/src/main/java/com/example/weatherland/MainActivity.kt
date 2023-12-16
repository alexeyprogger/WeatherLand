package com.example.weatherland

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.weatherland.fragments.MainFragment

// Класс, представляющий собой отдельный экран приложения (наследуемся от AppCompatActivity)
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportFragmentManager.beginTransaction().replace(R.id.placeHolder, MainFragment.newInstance()).commit()
    }
}
// С помощью newInstance создаём новый фрагмент MainFragment (приветственный экран "Добро пожаловать в WeatherLand!")
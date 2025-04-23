package com.example.uwasting.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

import com.example.uwasting.data.User
import com.example.uwasting.preferences.MyPreference

/**
 * Активность настроек.
 * Отвечает за установку языка приложения и возврат на главный экран.
 */
class SettingsActivity : AppCompatActivity() {

    /** Объект для работы с пользовательскими предпочтениями */
    lateinit var myPreference: MyPreference

    /** Контекст активности */
    lateinit var context: Context

    /**
     * Метод вызывается при создании активности.
     * Получает язык из аргументов Intent, сохраняет его в SharedPreferences и переходит в MainActivity.
     *
     * Ожидаемый параметр в `intent.extras`:
     * - "language": код языка (например, "en" или "ru")
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context = this

        val arguments = intent.extras
        var language: String

        // Получение языка из переданных аргументов
        arguments!!.let {
            language = it.getString("language", "en")
        }

        // Сохранение выбранного языка в локальных настройках
        myPreference = MyPreference(this)
        myPreference.setLanguage(language)

        // Переход на главный экран
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}

package com.example.uwasting.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.uwasting.R
import com.example.uwasting.data.Constants
import com.example.uwasting.data.OnBackButtonListener
import com.example.uwasting.data.User
import com.example.uwasting.data.remote.UWastingApi
import com.example.uwasting.fragments.StartFragment
import com.example.uwasting.preferences.MyPreference
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory


/**
 * Начальная активность приложения UWasting.
 * Проверяет, авторизован ли пользователь, и перенаправляет на главный экран или отображает экран авторизации.
 */
class StartingActivity : AppCompatActivity() {

    /** Объект пользователя, полученный из локального хранилища */
    var user: User = User()

    /** Класс для доступа к сохранённым пользовательским настройкам */
    lateinit var myPreference: MyPreference

    /** Интерфейс для работы с серверным API */
    lateinit var uwastingApi: UWastingApi

    /**
     * Метод инициализации активности.
     * Настраивает API, загружает пользователя и выбирает стартовый экран.
     * Если пользователь уже авторизован, происходит переход в [MainActivity].
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        configureRetrofit()

        myPreference = MyPreference(this)
        user = myPreference.getUser()

        // Если пользователь авторизован — сразу переходим в MainActivity
        if (user.id != -1) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        // Иначе — показываем фрагмент начала (вход/регистрация)
        setFragment(StartFragment())
    }

    /**
     * Настраивает подключение к серверу через Retrofit.
     * Добавляет логирование всех HTTP-запросов.
     */
    private fun configureRetrofit() {
        val httpLoggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(httpLoggingInterceptor)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(Constants.APIUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()

        uwastingApi = retrofit.create(UWastingApi::class.java)
    }

    /**
     * Загружает указанный фрагмент в контейнер.
     *
     * @param fragment Фрагмент для отображения.
     */
    fun setFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(fragment.tag)
            .commit()
    }

    /**
     * Возвращает предыдущий фрагмент из стека навигации.
     */
    fun prevFragment() {
        supportFragmentManager.popBackStack()
    }

    /**
     * Обрабатывает нажатие кнопки "Назад".
     * Делегирует действие текущему фрагменту, если он реализует [OnBackButtonListener].
     */
    override fun onBackPressed() {
        val backStackCount = supportFragmentManager.backStackEntryCount

        if (backStackCount > 0) {
            val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
            if (currentFragment is OnBackButtonListener) {
                val actionResult = currentFragment.onBackPressed()
                if (actionResult) return
            }
        }

        super.onBackPressed()
    }
}

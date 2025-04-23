@file:Suppress("DEPRECATION", "NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")

package com.example.uwasting.activities

import MyContextWrapper
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.uwasting.R
import com.example.uwasting.data.*
import com.example.uwasting.data.LocalDateDeserializer
import com.example.uwasting.data.remote.UWastingApi
import com.example.uwasting.fragments.CREATE_FILE_EXPENSES
import com.example.uwasting.fragments.CREATE_FILE_INCOMES
import com.example.uwasting.fragments.TabFragment
import com.example.uwasting.preferences.MyPreference
import com.google.gson.GsonBuilder
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.time.LocalDate

/**
 * Главная активность приложения UWasting.
 * Отвечает за инициализацию, работу с данными пользователя, переключение фрагментов и экспорт операций в CSV.
 */
@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {

    /** Авторизованный пользователь */
    lateinit var user: User

    /** Коллекция для управления подписками RxJava */
    private val compositeDisposable = CompositeDisposable()

    /** Интерфейс для работы с серверным API */
    lateinit var uwastingApi: UWastingApi

    /** Класс для работы с локальными настройками и предпочтениями */
    lateinit var myPreference: MyPreference

    /** Все операции пользователя */
    lateinit var totalOperations: OperationsList

    /** Операции за выбранный период */
    lateinit var currentOperations: OperationsList

    /** Количество дней для анализа операций (например, 30 дней) */
    var period = 30

    /** Текущая валюта (по умолчанию — рубль) */
    var curr = "₽"

    /** Условная единица (используется при конвертации валют, если реализовано) */
    var ue = 1

    /** Индекс текущей вкладки (для графиков или статистики) */
    var index = 0f

    /**
     * Переопределение базового контекста для поддержки локализации.
     */
    override fun attachBaseContext(newBase: Context?) {
        myPreference = MyPreference(newBase!!)
        val lang = myPreference.getLanguage()
        super.attachBaseContext(MyContextWrapper.wrap(newBase, lang))
    }

    /**
     * Обновляет список текущих операций, отбирая их из общего списка по заданному периоду.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun updateCurrentOperations() {
        currentOperations = OperationsList(ArrayList(totalOperations.selectOperations(period)))
    }

    /**
     * Обрабатывает результат выбора файла пользователем (экспорт в CSV).
     */
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode != CREATE_FILE_INCOMES && requestCode != CREATE_FILE_EXPENSES || resultCode != RESULT_OK) return

        val operations = if (requestCode == CREATE_FILE_INCOMES) currentOperations.selectOperationsIncomes()
        else currentOperations.selectOperationsExpenses()

        val selectedFile = data?.data

        if (selectedFile != null) {
            val writer = contentResolver.openOutputStream(selectedFile)?.bufferedWriter()
            val csvPrinter = CSVPrinter(writer, CSVFormat.DEFAULT.withHeader("OperationId", "Category", "Amount", "Date"))

            for (operation in operations) {
                val operationData = listOf(operation.id, operation.category, operation.amount, operation.date)
                csvPrinter.printRecord(operationData)
            }

            csvPrinter.flush()
            csvPrinter.close()

            val intent = Intent(Intent.ACTION_VIEW)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.setDataAndType(selectedFile, "text/csv")
            startActivity(Intent.createChooser(intent, "Open"))
        }
    }

    /**
     * Загружает операции пользователя с сервера.
     * В случае ошибки возвращает на экран авторизации.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun getOperations() {
        uwastingApi.let {
            compositeDisposable.add(uwastingApi.getOperations(user.id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    totalOperations = OperationsList(it)
                    updateCurrentOperations()
                    setFragment(TabFragment())
                }, {
                    user.id = -1
                    myPreference.setUser(user)
                    startActivity(Intent(this, StartingActivity::class.java))
                    finish()
                }))
        }
    }

    /**
     * Основной метод инициализации активности.
     * Загружает пользователя, настраивает API и запускает загрузку операций.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        myPreference = MyPreference(this)
        user = myPreference.getUser()
        configureRetrofit()
        getOperations()
    }

    /**
     * Проверяет наличие разрешений (например, на запись в память).
     *
     * @param permission Разрешение, которое требуется проверить
     * @param requestCode Код запроса разрешения
     */
    fun checkPermission(permission: String, requestCode: Int) {
        if (ContextCompat.checkSelfPermission(this@MainActivity, permission) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this@MainActivity, arrayOf(permission), requestCode)
        }
    }

    /**
     * Настраивает подключение к серверу с помощью Retrofit.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun configureRetrofit() {
        val gson = GsonBuilder().registerTypeAdapter(LocalDate::class.java, LocalDateDeserializer()).create()
        val httpLoggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(httpLoggingInterceptor)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(Constants.APIUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()

        uwastingApi = retrofit.create(UWastingApi::class.java)
    }

    /**
     * Заменяет текущий фрагмент на указанный.
     *
     * @param fragment Новый фрагмент для отображения
     */
    fun setFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(fragment.tag)
            .commit()
    }

    /**
     * Возвращает предыдущий фрагмент из backstack.
     */
    fun prevFragment() {
        supportFragmentManager.popBackStack()
    }

    /**
     * Обработка нажатия кнопки "Назад".
     * Передаёт событие текущему фрагменту, если он реализует [OnBackButtonListener].
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

@file:Suppress("DEPRECATION")

package com.example.uwasting.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.DocumentsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.uwasting.R
import com.example.uwasting.activities.MainActivity
import com.example.uwasting.data.Category
import com.example.uwasting.data.CategoryRecyclerView
import com.example.uwasting.data.Constants
import com.example.uwasting.data.OnItemClickListener
import com.example.uwasting.data.remote.StatBureauApi
import com.example.uwasting.dialogs.PeriodDialog
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.google.android.material.button.MaterialButton
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
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.math.pow
import kotlin.math.round

const val CREATE_FILE_INCOMES = 111
const val CREATE_FILE_EXPENSES = 112

interface UpdateFragment{
    fun update()
}
/**
 * Фрагмент, отображающий информацию о доходах пользователя.
 *
 * Содержит:
 * - список категорий доходов с количеством и суммами;
 * - круговую диаграмму;
 * - расчёт баланса с учётом инфляции (на основе данных StatBureau);
 * - экспорт данных в CSV;
 * - выбор временного периода анализа.
 */
class IncomesFragment : Fragment(), OnItemClickListener, UpdateFragment {

    private lateinit var pieChart: PieChart
    private lateinit var totalIncomesTxt: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var dateTxt: TextView
    private lateinit var balanceView: TextView
    private lateinit var mainActivity: MainActivity
    private lateinit var statBureauApi: StatBureauApi
    private var compositeDisposable = CompositeDisposable()

    /**
     * Обработка нажатия на элемент категории.
     * Переход к [CategoryFragment] с операциями данной категории доходов.
     */
    override fun onItemClicked(item: Triple<Category, Int, Int>) {
        mainActivity.setFragment(CategoryFragment(item.first, true))
    }

    /**
     * Настройка и выполнение запроса к [StatBureauApi] для получения текущей инфляции.
     * Используется для корректного расчёта реального баланса.
     */
    @SuppressLint("SetTextI18n")
    private fun configureRetrofit() {
        val httpLoggingInterceptor = HttpLoggingInterceptor()
        httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(httpLoggingInterceptor)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(Constants.StatBureauUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()

        statBureauApi = retrofit.create(StatBureauApi::class.java)

        compositeDisposable.add(
            statBureauApi.getIndex("russia")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->
                    mainActivity.index = result[0].inflationRate
                    updateBalance()
                }, {
                    // Ошибка загрузки индекса инфляции — можно логировать
                })
        )
    }

    /**
     * Обновление визуального интерфейса доходов.
     * Включает диаграмму, список категорий, расчёт баланса.
     */
    @SuppressLint("SetTextI18n")
    fun updateOperations() {
        val sumIncomes = mainActivity.currentOperations?.getTotalSumIncomes()

        if (sumIncomes != null) {
            totalIncomesTxt.text = "+${round(sumIncomes.toFloat() / mainActivity.ue * 100) / 100.0}${mainActivity.curr}"
        }

        recyclerView.layoutManager = LinearLayoutManager(mainActivity)
        recyclerView.adapter = mainActivity.currentOperations?.let {
            CategoryRecyclerView(
                it.combineByCategoryIncomes(),
                this,
                mainActivity
            )
        }

        loadPieChartData()
        updateBalance()
    }

    /**
     * Пересчёт и обновление баланса с учётом инфляции.
     */
    private fun updateBalance() {
        val sumIncomes = mainActivity.currentOperations?.getTotalSumIncomes()
        val sumExpenses = mainActivity.currentOperations?.getTotalSumExpenses()
        val inflationFactor = ((mainActivity.index + 100) / 100).pow(mainActivity.period / 30)
        val balance = (sumIncomes?.plus(sumExpenses!!))?.div(inflationFactor)

        if (balance != null) {
            balanceView.text = getString(R.string.balance) + " " +
                    String.format("%.2f", balance / mainActivity.ue) + mainActivity.curr
        }
    }

    /**
     * Основной метод создания представления фрагмента.
     * Выполняет инициализацию UI и настройку событий.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n", "SdCardPath")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mainActivity = activity as MainActivity
        val view = inflater.inflate(R.layout.fragment_incomes, container, false)

        if (mainActivity.index == 0f) configureRetrofit()

        // Инициализация виджетов
        totalIncomesTxt = view.findViewById(R.id.sum_txt)
        val exportToCSVBtn = view.findViewById<Button>(R.id.export_btn)
        val periodLayout = view.findViewById<ConstraintLayout>(R.id.period_layout)
        val addIncomeBtn = view.findViewById<MaterialButton>(R.id.add_income_btn)
        balanceView = view.findViewById(R.id.balance_inc)
        dateTxt = view.findViewById(R.id.date_txt)
        recyclerView = view.findViewById(R.id.categories_list)
        pieChart = view.findViewById(R.id.diagram_incomes)

        dateTxt.text = getString(R.string.last) + " ${mainActivity.period} " + getString(R.string.days)

        setupPieChart()
        updateOperations()

        // События
        periodLayout.setOnClickListener {
            PeriodDialog(mainActivity, this).show(parentFragmentManager, "period")
        }

        addIncomeBtn.setOnClickListener {
            mainActivity.setFragment(NewIncomeFragment())
        }

        exportToCSVBtn.setOnClickListener {
            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "text/csv"
                putExtra(Intent.EXTRA_TITLE, "incomes.csv")
            }
            mainActivity.startActivityForResult(intent, CREATE_FILE_INCOMES)
        }

        return view
    }

    /**
     * Настраивает внешний вид круговой диаграммы.
     */
    private fun setupPieChart() {
        pieChart.isDrawHoleEnabled = false
        pieChart.setUsePercentValues(true)
        pieChart.setDrawEntryLabels(false)
        pieChart.centerText = ""
        pieChart.description.isEnabled = false

        pieChart.legend.apply {
            verticalAlignment = Legend.LegendVerticalAlignment.TOP
            horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
            orientation = Legend.LegendOrientation.VERTICAL
            setDrawInside(false)
            isEnabled = false
        }
    }

    /**
     * Загружает данные по доходам в круговую диаграмму.
     */
    private fun loadPieChartData() {
        val operations = mainActivity.currentOperations?.combineByCategoryIncomes()
        val entries = ArrayList<PieEntry>()
        val colors = ArrayList<Int>()

        if (operations != null) {
            for (i in operations) {
                entries.add(PieEntry(i.third.toFloat(), i.first.name))
                colors.add(i.first.color)
            }
        }

        val dataSet = PieDataSet(entries, "").apply { this.colors = colors }
        val data = PieData(dataSet).apply { setDrawValues(false) }

        pieChart.data = data
        pieChart.invalidate()
        pieChart.animateY(1000, Easing.EaseInOutQuad)
    }

    /**
     * Вызывается при обновлении периода анализа.
     * Обновляет интерфейс и данные.
     */
    @SuppressLint("SetTextI18n")
    override fun update() {
        dateTxt.text = "Последние ${mainActivity.period} дней"
        updateOperations()
    }
}

package com.example.uwasting.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.uwasting.R
import com.example.uwasting.activities.MainActivity
import com.example.uwasting.data.*

import com.example.uwasting.dialogs.PeriodDialog
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.math.abs
import kotlin.math.round

/**
 * Фрагмент, отображающий статистику расходов пользователя.
 *
 * Содержит:
 * - диаграмму по категориям;
 * - список категорий с суммами;
 * - прогноз трат на основе линейной регрессии;
 * - общую сумму расходов;
 * - возможность экспорта в CSV;
 * - выбор периода анализа.
 */
class ExpensesFragment : Fragment(), OnItemClickListener, UpdateFragment {

    private lateinit var pieChart: PieChart
    private lateinit var mainActivity: MainActivity
    private lateinit var dateTxt: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var totalExpensesTxt: TextView
    private lateinit var forecastView: TextView

    /**
     * Обновляет список расходов, диаграмму и прогноз.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n")
    fun updateOperations() {
        loadPieChartData()

        val total = mainActivity.currentOperations.getTotalSumExpenses()
        totalExpensesTxt.text = "${round(total.toFloat() / mainActivity.ue * 100) / 100.0}${mainActivity.curr}"

        val expenses = mainActivity.currentOperations.selectOperationsExpenses()
        val lineReg = LineReg(ArrayList(expenses))
        val prediction = lineReg.evaluateAlgorithm()

        forecastView.text = getString(R.string.monthly_forecast) + ": " +
                String.format("%.2f", prediction / mainActivity.ue) + mainActivity.curr

        recyclerView.layoutManager = LinearLayoutManager(mainActivity)
        recyclerView.adapter = CategoryRecyclerView(
            mainActivity.currentOperations.combineByCategoryExpenses(),
            this,
            mainActivity
        )
    }

    /**
     * Создаёт и возвращает представление фрагмента.
     * Настраивает интерфейс и запускает начальное обновление данных.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_expenses, container, false)
        mainActivity = activity as MainActivity

        // Инициализация компонентов
        val exportToCSVBtn = view.findViewById<Button>(R.id.export_btn)
        val addExpenseBtn = view.findViewById<Button>(R.id.add_expense_btn)
        val periodLayout = view.findViewById<ConstraintLayout>(R.id.period_layout)
        recyclerView = view.findViewById(R.id.categories_list)
        dateTxt = view.findViewById(R.id.date_txt)
        totalExpensesTxt = view.findViewById(R.id.totalExpenses)
        forecastView = view.findViewById(R.id.forecast)
        pieChart = view.findViewById(R.id.diagram_expenses)

        dateTxt.text = getString(R.string.last) + " ${mainActivity.period} " + getString(R.string.days)

        setupPieChart()
        updateOperations()

        // Выбор периода анализа
        periodLayout.setOnClickListener {
            val dialog = PeriodDialog(mainActivity, this)
            dialog.show(parentFragmentManager, "period")
        }

        // Добавление новой операции
        addExpenseBtn.setOnClickListener {
            mainActivity.setFragment(NewExpenseFragment())
        }

        // Экспорт в CSV
        exportToCSVBtn.setOnClickListener {
            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "text/csv"
                putExtra(Intent.EXTRA_TITLE, "expenses.csv")
            }
            mainActivity.startActivityForResult(intent, CREATE_FILE_EXPENSES)
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
     * Загружает данные расходов по категориям в круговую диаграмму.
     */
    private fun loadPieChartData() {
        val operations = mainActivity.currentOperations.combineByCategoryExpenses()
        val entries = ArrayList<PieEntry>()
        val colors = ArrayList<Int>()

        for (operation in operations) {
            entries.add(PieEntry(-operation.third.toFloat(), operation.first.name))
            colors.add(operation.first.color)
        }

        val dataSet = PieDataSet(entries, "").apply {
            this.colors = colors
        }

        val data = PieData(dataSet).apply {
            setDrawValues(false)
        }

        pieChart.data = data
        pieChart.invalidate()
        pieChart.animateY(1000, Easing.EaseInOutQuad)
    }

    /**
     * Обработка нажатия на элемент категории — переход к [CategoryFragment].
     */
    override fun onItemClicked(item: Triple<Category, Int, Int>) {
        mainActivity.setFragment(CategoryFragment(item.first, false))
    }

    /**
     * Обновляет интерфейс после изменения периода.
     */
    @SuppressLint("SetTextI18n")
    override fun update() {
        dateTxt.text = "Последние ${mainActivity.period} дней"
        updateOperations()
    }
}

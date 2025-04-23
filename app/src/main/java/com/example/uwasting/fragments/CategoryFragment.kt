package com.example.uwasting.fragments

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.uwasting.R
import com.example.uwasting.activities.MainActivity
import com.example.uwasting.data.Category
import com.example.uwasting.data.OnOperationClickListener
import com.example.uwasting.data.OperationsList
import com.example.uwasting.data.OperationsRecyclerView
import com.example.uwasting.dialogs.OperationDialog
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.appbar.MaterialToolbar
import java.time.LocalDate

/**
 * Интерфейс обратного вызова, вызываемого после установки новой базовой операции.
 * Используется, чтобы обновить UI после изменения валюты (у.е.) или удаления операции.
 */
interface OnSetBaseOperation {
    /**
     * Вызывается для обновления отображения при изменении данных базовой операции.
     */
    fun onSet()
}

/**
 * Кастомный форматтер оси X для отображения дат на [BarChart].
 *
 * @param xValsDateLabel Список меток по оси X — дат операций.
 */
class ValueFormatter(private val xValsDateLabel: ArrayList<LocalDate>) : com.github.mikephil.charting.formatter.ValueFormatter() {

    override fun getFormattedValue(value: Float): String {
        return value.toString()
    }

    override fun getAxisLabel(value: Float, axis: com.github.mikephil.charting.components.AxisBase): String {
        return if (value.toInt() in xValsDateLabel.indices) {
            xValsDateLabel[value.toInt()].toString()
        } else {
            ""
        }
    }
}

/**
 * Фрагмент, отображающий операции выбранной категории и соответствующую диаграмму.
 * Используется как детализация внутри вкладок доходов или расходов.
 *
 * @param category Категория, для которой отображаются операции.
 * @param income Флаг: `true`, если категория относится к доходам, иначе — к расходам.
 */
class CategoryFragment(
    private var category: Category,
    private var income: Boolean
) : Fragment(), OnOperationClickListener, OnSetBaseOperation {

    /** Диаграмма для отображения суммы операций по датам */
    lateinit var barChart: BarChart

    /** Список операций, отфильтрованный по категории */
    lateinit var listOperations: OperationsList

    /** Список (RecyclerView) с операциями */
    lateinit var recyclerView: RecyclerView

    /** Активность, в которой расположен фрагмент */
    lateinit var mainActivity: MainActivity

    /**
     * Устанавливает список операций для текущей категории, с учётом фильтра по доходам/расходам.
     */
    fun setListOperations() {
        listOperations = mainActivity.currentOperations.selectByCategory(category)
        listOperations = if (income) {
            OperationsList(listOperations.selectOperationsIncomes())
        } else {
            OperationsList(listOperations.selectOperationsExpenses())
        }

        if (listOperations.list.isEmpty()) mainActivity.prevFragment()
    }

    /**
     * Настраивает список операций и диаграмму.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun setValues() {
        recyclerView.adapter = OperationsRecyclerView(listOperations.sortByDate(), this, mainActivity)

        val barDataSet = BarDataSet(getEntries(listOperations), "")
        barDataSet.colors = listOf(category.color)

        val barData = BarData(barDataSet).apply {
            barWidth = 0.5f
        }

        barChart.data = barData
        barChart.xAxis.valueFormatter = ValueFormatter(ArrayList(listOperations.combineByDateIncomesAndExpenses().keys))
        barChart.invalidate()
    }

    /**
     * Создаёт и возвращает представление фрагмента.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_category, container, false)
        mainActivity = activity as MainActivity
        setListOperations()

        val toolbar = view.findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
        recyclerView = view.findViewById(R.id.operations_list)
        recyclerView.layoutManager = LinearLayoutManager(mainActivity)

        barChart = view.findViewById(R.id.operations_barchart)
        barChart.xAxis.granularity = 1f
        barChart.description.isEnabled = false
        barChart.legend.isEnabled = false
        barChart.animateY(300)

        setValues()

        toolbar.setNavigationOnClickListener {
            mainActivity.prevFragment()
        }

        return view
    }

    /**
     * Формирует данные для диаграммы — список точек по датам.
     *
     * @param list Список операций.
     * @return Список значений для [BarChart].
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun getEntries(list: OperationsList): ArrayList<BarEntry> {
        val tmp = list.combineByDateIncomesAndExpenses()
        val dataVals = ArrayList<BarEntry>()
        var cnt = 0f

        for (i in tmp) {
            val value = if (income) i.value.first.toFloat() else kotlin.math.abs(i.value.second.toFloat())
            dataVals.add(BarEntry(cnt, value))
            cnt += 1
        }

        return dataVals
    }

    /**
     * Обрабатывает нажатие на элемент списка операций.
     *
     * @param item Тройка (дата, категория, сумма) — данные выбранной операции.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onItemClick(item: Triple<LocalDate, Category, Int>) {
        val mainActivity = activity as MainActivity
        val operationDialog = context?.let {
            OperationDialog(
                it,
                mainActivity.currentOperations.findOperation(item.first, item.third, item.second),
                item.third,
                mainActivity,
                this
            )
        }
        operationDialog?.show()
    }

    /**
     * Метод из интерфейса [OnSetBaseOperation], вызываемый после обновления операций.
     * Обновляет список и диаграмму.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onSet() {
        setListOperations()
        setValues()
    }
}

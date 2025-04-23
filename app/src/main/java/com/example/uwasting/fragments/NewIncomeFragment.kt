package com.example.uwasting.fragments

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.example.uwasting.R
import com.example.uwasting.activities.MainActivity
import com.example.uwasting.data.Constants
import com.example.uwasting.data.remote.UWastingApi
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textfield.TextInputEditText
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

interface SetCategory{
    fun setCategory(category:String)
}
/**
 * Фрагмент для добавления новой доходной операции.
 *
 * Пользователь указывает сумму, категорию и дату, после чего данные отправляются на сервер.
 * Также реализована поддержка выбора категории через фрагмент [SelectCategoryFragment].
 */
class NewIncomeFragment : Fragment(), SetCategory {

    /**
     * Текущая дата, используемая по умолчанию в выборе даты.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    val time: LocalDate = LocalDate.now()

    @RequiresApi(Build.VERSION_CODES.O) var curYear = time.year
    @RequiresApi(Build.VERSION_CODES.O) var curMonth = time.monthValue - 1
    @RequiresApi(Build.VERSION_CODES.O) var curDay = time.dayOfMonth

    /** Поле ввода даты */
    lateinit var dateTxt: TextInputEditText

    /** Поле ввода категории */
    lateinit var categoryText: TextInputEditText

    /** Выбранная категория */
    private var category: String = ""

    /** Флаг для блокировки повторной отправки */
    private var isSendingOperation = false

    /** API-интерфейс сервера */
    private lateinit var uwastingApi: UWastingApi

    /** Коллекция подписок для RxJava */
    var compositeDisposable = CompositeDisposable()

    /**
     * Создание интерфейса фрагмента.
     * Настраивает поля ввода, кнопку добавления, выбор даты и категории.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_new_income, container, false)
        val mainActivity = activity as MainActivity
        uwastingApi = mainActivity.uwastingApi

        // Инициализация элементов UI
        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbar)
        categoryText = view.findViewById(R.id.category_edit)
        val addBtn = view.findViewById<Button>(R.id.add_btn)
        val amountTxt = view.findViewById<TextInputEditText>(R.id.sum_edit)
        dateTxt = view.findViewById(R.id.cmsn_edit)

        // Обработка нажатия кнопки "Добавить"
        addBtn.setOnClickListener {
            if (amountTxt.text.toString().isNotBlank()
                && categoryText.text.toString().isNotBlank()
                && dateTxt.text.toString().isNotBlank()
            ) {
                addIncome(amountTxt.text.toString().toInt(), categoryText.text.toString(), dateTxt.text.toString())
            } else {
                Toast.makeText(mainActivity, getString(R.string.field_is_empty), Toast.LENGTH_LONG).show()
            }
        }

        // Открытие календаря при выборе даты
        dateTxt.setOnClickListener {
            DatePickerDialog(mainActivity, callBack, curYear, curMonth, curDay).show()
        }

        // Выбор категории
        categoryText.setOnClickListener {
            mainActivity.setFragment(SelectCategoryFragment(this, Constants.INCOMES))
        }

        // Обработка кнопки "Назад"
        toolbar.setNavigationOnClickListener {
            mainActivity.prevFragment()
        }

        return view
    }

    /**
     * Обработка выбора даты в диалоге [DatePickerDialog].
     * Устанавливает выбранную дату в поле `dateTxt`.
     */
    @SuppressLint("SetTextI18n", "SimpleDateFormat")
    var callBack = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
        curYear = year
        curMonth = monthOfYear
        curDay = dayOfMonth

        val format = SimpleDateFormat("MM-dd-yyyy", Locale.ENGLISH)
        val calendar = Calendar.getInstance().apply {
            set(curYear, curMonth, curDay)
        }

        dateTxt.setText(format.format(calendar.time))
    }

    /**
     * Отправка данных о доходе на сервер через [UWastingApi].
     *
     * @param amount Сумма дохода.
     * @param category Название категории.
     * @param date Дата операции в формате "MM-dd-yyyy".
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun addIncome(amount: Int, category: String, date: String) {
        if (isSendingOperation) return
        val mainActivity = activity as MainActivity
        isSendingOperation = true

        compositeDisposable.add(
            uwastingApi.addOperation(amount, category, date, mainActivity.user.id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ id ->
                    if (id != -1) {
                        mainActivity.totalOperations.addOperation(
                            amount,
                            category,
                            LocalDate.parse(date, DateTimeFormatter.ofPattern("MM-dd-yyyy", Locale.ENGLISH)),
                            id
                        )
                        mainActivity.updateCurrentOperations()
                        mainActivity.prevFragment()
                    }
                }, {
                    Toast.makeText(mainActivity, getString(R.string.add_error), Toast.LENGTH_LONG).show()
                    isSendingOperation = false
                })
        )
    }

    /**
     * При возврате к фрагменту обновляет текст категории, если она уже выбрана.
     */
    override fun onResume() {
        super.onResume()
        categoryText.setText(category)
    }

    /**
     * Устанавливает выбранную категорию.
     *
     * @param category Название категории, выбранное пользователем.
     */
    override fun setCategory(category: String) {
        this.category = category
    }
}

package com.example.uwasting.fragments

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
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
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textfield.TextInputEditText
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * Фрагмент для добавления новой расходной операции.
 *
 * Содержит:
 * - поле ввода суммы;
 * - выбор категории;
 * - выбор даты;
 * - кнопку отправки;
 * - валидацию ввода;
 * - добавление операции через API.
 */
class NewExpenseFragment : Fragment(), SetCategory {

    /**
     * Текущая дата, используемая по умолчанию в выборе даты.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    val time: LocalDate = LocalDate.now()

    @RequiresApi(Build.VERSION_CODES.O) var myYear = time.year
    @RequiresApi(Build.VERSION_CODES.O) var myMonth = time.monthValue - 1
    @RequiresApi(Build.VERSION_CODES.O) var myDay = time.dayOfMonth

    /** Поле ввода даты */
    lateinit var datetxt: TextInputEditText

    /** Поле ввода категории */
    lateinit var categoryEdit: TextInputEditText

    /** Контейнер подписок RxJava */
    var compositeDisposable = CompositeDisposable()

    /** Выбранная категория */
    private var category: String = ""

    /** Флаг блокировки повторной отправки */
    private var isSendingOperation = false

    /**
     * Отправка расходной операции на сервер через [MainActivity.uwastingApi].
     *
     * @param amount Сумма расхода.
     * @param category Категория.
     * @param date Дата операции в формате "MM-dd-yyyy".
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun sendOperation(amount: Int, category: String, date: String) {
        if (isSendingOperation) return
        val mainActivity = activity as MainActivity
        isSendingOperation = true

        compositeDisposable.add(
            mainActivity.uwastingApi.addOperation(-amount, category, date, mainActivity.user.id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (it != -1) {
                        mainActivity.totalOperations.addOperation(
                            -amount,
                            category,
                            LocalDate.parse(date, DateTimeFormatter.ofPattern("MM-dd-yyyy", Locale.ENGLISH)),
                            it
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
     * Колбэк выбора даты в диалоге [DatePickerDialog].
     * Устанавливает выбранную дату в поле `datetxt`.
     */
    @SuppressLint("SetTextI18n", "SimpleDateFormat")
    var myCallBack = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
        myYear = year
        myMonth = monthOfYear
        myDay = dayOfMonth

        val format = SimpleDateFormat("MM-dd-yyyy", Locale.ENGLISH)
        val calendar = Calendar.getInstance().apply {
            set(myYear, myMonth, myDay)
        }

        datetxt.setText(format.format(calendar.time))
    }

    /**
     * Создание и отображение фрагмента.
     * Настраивает поля, обработчики кнопок, выбор категории и даты.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_new_expense, container, false)
        val mainActivity = activity as MainActivity

        // Получение элементов интерфейса
        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbar)
        categoryEdit = view.findViewById(R.id.category_edit)
        val addBtn = view.findViewById<Button>(R.id.add_btn)
        val amountTxt = view.findViewById<TextInputEditText>(R.id.sum_edit)
        datetxt = view.findViewById(R.id.cmsn_edit)

        // Обработка кнопки "Добавить"
        addBtn.setOnClickListener {
            if (amountTxt.text.toString().isNotBlank()
                && categoryEdit.text.toString().isNotBlank()
                && datetxt.text.toString().isNotBlank()
            ) {
                sendOperation(amountTxt.text.toString().toInt(), categoryEdit.text.toString(), datetxt.text.toString())
            } else {
                Toast.makeText(mainActivity, getString(R.string.field_is_empty), Toast.LENGTH_LONG).show()
            }
        }

        // Открытие календаря при выборе даты
        datetxt.setOnClickListener {
            DatePickerDialog(mainActivity, myCallBack, myYear, myMonth, myDay).show()
        }

        // Назад
        toolbar.setNavigationOnClickListener {
            mainActivity.prevFragment()
        }

        // Выбор категории
        categoryEdit.setOnClickListener {
            mainActivity.setFragment(SelectCategoryFragment(this, Constants.EXPENSES))
        }

        return view
    }

    /**
     * При возврате к фрагменту устанавливает сохранённую категорию в поле.
     */
    override fun onResume() {
        super.onResume()
        categoryEdit.setText(category)
    }

    /**
     * Установка выбранной категории (через интерфейс [SetCategory]).
     *
     * @param category Название категории.
     */
    override fun setCategory(category: String) {
        this.category = category
    }
}

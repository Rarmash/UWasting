package com.example.uwasting.dialogs

import android.content.Context
import android.os.Build
import android.widget.Button
import androidx.annotation.RequiresApi
import com.example.uwasting.R
import com.example.uwasting.activities.MainActivity
import com.example.uwasting.fragments.OnSetBaseOperation
import com.google.android.material.bottomsheet.BottomSheetDialog
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlin.math.abs

/**
 * Диалоговое окно управления операцией, отображаемое в виде нижнего листа ([BottomSheetDialog]).
 *
 * Предоставляет пользователю две возможности:
 * 1. Удалить операцию из базы данных и из интерфейса.
 * 2. Назначить операцию базовой валютой для отображения всех сумм в "у.е." (условных единицах).
 *
 * @param context Контекст, в котором отображается диалог.
 * @param id Уникальный идентификатор операции, с которой производится работа.
 * @param amount Сумма операции.
 * @param mainActivity Ссылка на [MainActivity] для доступа к API, данным и интерфейсу.
 * @param onSetBaseOperation Интерфейс, вызываемый при изменении базовой валюты (у.е.).
 */
@RequiresApi(Build.VERSION_CODES.O)
class OperationDialog(
    context: Context,
    private var id: Int,
    private var amount: Int,
    private var mainActivity: MainActivity,
    private var onSetBaseOperation: OnSetBaseOperation
) : BottomSheetDialog(context) {

    /** Коллекция подписок RxJava */
    private var compositeDisposable = CompositeDisposable()

    /** Флаг, исключающий повторное удаление при нажатии */
    private var isDeletingOperation = false

    init {
        setContentView(R.layout.dialog_operation)

        val deleteBtn = findViewById<Button>(R.id.delete_btn)
        val makeBase = findViewById<Button>(R.id.make_base_btn)

        // Удаление операции
        deleteBtn?.setOnClickListener {
            if (!isDeletingOperation) {
                isDeletingOperation = true
                mainActivity.uwastingApi.let {
                    compositeDisposable.add(
                        mainActivity.uwastingApi.deleteOperation(id)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({ success ->
                                if (success) {
                                    mainActivity.totalOperations.removeOperation(id)
                                    mainActivity.updateCurrentOperations()
                                    onSetBaseOperation.onSet()
                                    this.dismiss()
                                }
                            }, {
                                isDeletingOperation = false
                            })
                    )
                }
            }
        }

        // Установка текущей операции как базовой валюты (у.е.)
        makeBase?.setOnClickListener {
            mainActivity.curr = "у.е."
            mainActivity.ue = abs(amount)
            onSetBaseOperation.onSet()
            this.dismiss()
        }
    }
}
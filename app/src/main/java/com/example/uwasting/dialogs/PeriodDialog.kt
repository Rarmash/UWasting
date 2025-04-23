package com.example.uwasting.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.fragment.app.DialogFragment
import com.example.uwasting.R
import com.example.uwasting.activities.MainActivity
import com.example.uwasting.fragments.UpdateFragment

/**
 * Диалоговое окно для выбора периода отображения операций.
 *
 * Отображается как [DialogFragment] и позволяет пользователю выбрать один из временных интервалов:
 * - 1 месяц
 * - 1 квартал
 * - Полгода
 * - 1 год
 *
 * После выбора обновляется список отображаемых операций и вызывается обновление фрагмента.
 *
 * @param mainActivity Ссылка на [MainActivity], где хранятся операции и текущий период.
 * @param updateFragment Фрагмент, реализующий интерфейс [UpdateFragment] для обновления UI.
 */
class PeriodDialog(
    private var mainActivity: MainActivity,
    private var updateFragment: UpdateFragment
) : DialogFragment() {

    /**
     * Создаёт диалог с кнопками выбора временного периода.
     *
     * @return [Dialog] с пользовательским представлением и логикой выбора.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
        val view = activity?.layoutInflater?.inflate(R.layout.dialog_period, null)

        val monthBtn = view?.findViewById<Button>(R.id.month_btn)
        val quarterBtn = view?.findViewById<Button>(R.id.quarter_btn)
        val halfYearBtn = view?.findViewById<Button>(R.id.half_year_btn)
        val yearBtn = view?.findViewById<Button>(R.id.year_btn)

        // Установка периода: 30 дней
        monthBtn?.setOnClickListener {
            mainActivity.period = 30
            mainActivity.updateCurrentOperations()
            updateFragment.update()
            this.dismiss()
        }

        // Установка периода: 90 дней (квартал)
        quarterBtn?.setOnClickListener {
            mainActivity.period = 90
            mainActivity.updateCurrentOperations()
            updateFragment.update()
            this.dismiss()
        }

        // Установка периода: 180 дней (полгода)
        halfYearBtn?.setOnClickListener {
            mainActivity.period = 180
            mainActivity.updateCurrentOperations()
            updateFragment.update()
            this.dismiss()
        }

        // Установка периода: 365 дней (год)
        yearBtn?.setOnClickListener {
            mainActivity.period = 365
            mainActivity.updateCurrentOperations()
            updateFragment.update()
            this.dismiss()
        }

        builder.setView(view)
        return builder.create()
    }
}

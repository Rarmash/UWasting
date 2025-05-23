package com.example.uwasting.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.widget.RadioButton
import androidx.fragment.app.DialogFragment
import com.example.uwasting.R
import com.example.uwasting.activities.MainActivity
import com.example.uwasting.activities.SettingsActivity
import com.example.uwasting.preferences.MyPreference

/**
 * Диалог выбора языка интерфейса.
 *
 * Отображается в виде [DialogFragment] и позволяет пользователю выбрать один из доступных языков (русский или английский).
 * После выбора производится перезапуск активности с передачей нового языка в [SettingsActivity].
 */
class LanguageDialog : DialogFragment() {

    /**
     * Создаёт диалоговое окно с двумя радиокнопками — для выбора языка (русский / английский).
     *
     * @return [Dialog] с пользовательским представлением.
     */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
        val mainActivity = activity as MainActivity
        val view = activity?.layoutInflater?.inflate(R.layout.dialog_language, null)

        val btnRus = view?.findViewById<RadioButton>(R.id.radioBtnRus)
        val btnEng = view?.findViewById<RadioButton>(R.id.radioBtnEng)
        val myPreference = MyPreference(mainActivity)

        // При выборе русского языка
        btnRus?.setOnClickListener {
            val myIntent = Intent(mainActivity, SettingsActivity::class.java)
            myIntent.putExtra("language", "ru")
            startActivity(myIntent)
            this.dismiss()
        }

        // При выборе английского языка
        btnEng?.setOnClickListener {
            val myIntent = Intent(mainActivity, SettingsActivity::class.java)
            myIntent.putExtra("language", "en")
            startActivity(myIntent)
            this.dismiss()
        }

        builder.setView(view)
        return builder.create()
    }
}

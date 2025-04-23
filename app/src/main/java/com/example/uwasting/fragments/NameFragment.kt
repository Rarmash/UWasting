package com.example.uwasting.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.uwasting.R
import com.example.uwasting.activities.StartingActivity
import com.google.android.material.appbar.MaterialToolbar

/**
 * Фрагмент для ввода имени и фамилии пользователя при регистрации.
 *
 * Отображается после ввода электронной почты. Пользователь вводит имя и фамилию,
 * после чего происходит переход к экрану ввода пароля ([PasswordFragment]).
 */
class NameFragment : Fragment() {

    /**
     * Создаёт и возвращает представление фрагмента.
     *
     * Настраивает поля ввода имени и фамилии, кнопку "Далее", а также кнопку "Назад" в тулбаре.
     *
     * @return Представление экрана.
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_name, container, false)
        val startingActivity = activity as StartingActivity

        val nameEdit = view.findViewById<EditText>(R.id.name_edit)       // Поле для ввода имени
        val surnameEdit = view.findViewById<EditText>(R.id.surname_edit) // Поле для ввода фамилии
        val nextBtn = view.findViewById<Button>(R.id.next_btn)           // Кнопка "Далее"
        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbar)   // Верхняя панель с кнопкой "Назад"

        // Обработка кнопки "Далее"
        nextBtn.setOnClickListener {
            if (nameEdit.text.toString().isBlank() || surnameEdit.text.toString().isBlank()) {
                val text = getString(R.string.field_is_empty)
                Toast.makeText(startingActivity, text, Toast.LENGTH_LONG).show()
            } else {
                // Сохраняем имя и фамилию в объект пользователя
                startingActivity.user.name = nameEdit.text.toString()
                startingActivity.user.surname = surnameEdit.text.toString()

                // Переход на фрагмент ввода пароля
                startingActivity.setFragment(PasswordFragment())
            }
        }

        // Обработка кнопки "Назад"
        toolbar.setNavigationOnClickListener {
            startingActivity.prevFragment()
        }

        return view
    }
}

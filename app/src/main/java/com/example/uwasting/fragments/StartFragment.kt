package com.example.uwasting.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.example.uwasting.R
import com.example.uwasting.activities.StartingActivity
import com.example.uwasting.data.OnBackButtonListener

/**
 * Стартовый фрагмент приложения.
 *
 * Отображает два варианта действий для пользователя:
 * - Вход в существующий аккаунт ([SignInFragment])
 * - Регистрация нового пользователя ([EmailFragment])
 *
 * Используется в [StartingActivity] как первый экран.
 */
class StartFragment : Fragment(), OnBackButtonListener {

    /**
     * Создание и отображение представления фрагмента.
     * Устанавливает обработчики для кнопок "Войти" и "Зарегистрироваться".
     *
     * @return View фрагмента.
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_start, container, false)
        val startingActivity: StartingActivity = activity as StartingActivity

        val signInBtn = view.findViewById<Button>(R.id.sign_in_btn)  // Кнопка входа
        val signUpBtn = view.findViewById<Button>(R.id.sign_up_btn)  // Кнопка регистрации

        // Обработка нажатия "Войти"
        signInBtn.setOnClickListener {
            startingActivity.setFragment(SignInFragment())
        }

        // Обработка нажатия "Зарегистрироваться"
        signUpBtn.setOnClickListener {
            startingActivity.setFragment(EmailFragment())
        }

        return view
    }

    /**
     * Переопределение поведения кнопки "Назад".
     * Возвращает `true`, чтобы отключить стандартное поведение.
     */
    override fun onBackPressed(): Boolean {
        return true
    }
}

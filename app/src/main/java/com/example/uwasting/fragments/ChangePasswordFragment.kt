package com.example.uwasting.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.example.uwasting.R
import com.example.uwasting.activities.MainActivity
import com.example.uwasting.data.remote.UWastingApi
import com.google.android.material.appbar.MaterialToolbar
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

/**
 * Фрагмент, предназначенный для смены пароля пользователя.
 *
 * Содержит два текстовых поля: для нового пароля и для его повторного ввода.
 * Проверяет совпадение введённых паролей и выполняет запрос на изменение пароля через [UWastingApi].
 */
class ChangePasswordFragment : Fragment() {

    /** Коллекция подписок для RxJava, используется для управления жизненным циклом запросов */
    private var compositeDisposable = CompositeDisposable()

    /**
     * Создаёт и возвращает представление фрагмента.
     *
     * Настраивает интерфейс: ввод нового пароля, повтор пароля, кнопка "Сохранить", переход назад.
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_change_password, container, false)
        val mainActivity = activity as MainActivity

        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbar)
        val addBtn = view.findViewById<Button>(R.id.add_btn)
        val pswrdTextView = view.findViewById<TextView>(R.id.new_pswrd_edit)
        val repPswrdTextView = view.findViewById<TextView>(R.id.repeat_pswrd_edit)

        // Обработка нажатия кнопки "Сохранить"
        addBtn.setOnClickListener {
            val password = pswrdTextView.text.toString()
            val repeat = repPswrdTextView.text.toString()

            if (password.isNotEmpty() && password == repeat) {
                changePassword(mainActivity.uwastingApi, password)
            } else if (password.isEmpty()) {
                val text = getString(R.string.field_is_empty)
                Toast.makeText(mainActivity, text, Toast.LENGTH_LONG).show()
            } else {
                val text = getString(R.string.passwords_dont_match)
                Toast.makeText(mainActivity, text, Toast.LENGTH_LONG).show()
            }
        }

        // Обработка кнопки "Назад"
        toolbar.setNavigationOnClickListener {
            mainActivity.prevFragment()
        }

        return view
    }

    /**
     * Отправляет запрос на изменение пароля через API.
     *
     * @param uwastingApi Интерфейс API приложения.
     * @param password Новый пароль пользователя.
     */
    private fun changePassword(uwastingApi: UWastingApi, password: String) {
        val mainActivity = activity as MainActivity

        compositeDisposable.add(
            uwastingApi.changePassword(mainActivity.user.id, password)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    // При успешной смене пароля переходим к основному экрану
                    mainActivity.setFragment(TabFragment())
                }, {
                    // В случае ошибки отображаем сообщение
                    val text = getString(R.string.impossible_to_change_password)
                    Toast.makeText(mainActivity, text, Toast.LENGTH_LONG).show()
                })
        )
    }
}

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
 * Фрагмент, отвечающий за смену электронной почты пользователя.
 *
 * Содержит поля для отображения текущей почты, ввода новой почты и кнопку "Сохранить".
 * Выполняет проверку заполненности поля и отправляет запрос на обновление e-mail через API.
 */
class ChangeEmailFragment : Fragment() {

    /** Коллекция подписок для RxJava, используемая для очистки ресурсов */
    private var compositeDisposable = CompositeDisposable()

    /**
     * Создаёт и возвращает представление фрагмента.
     *
     * Инициализирует UI: отображает текущую почту, настраивает кнопку изменения и кнопку "Назад".
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_change_email, container, false)
        val mainActivity = activity as MainActivity

        // Получение виджетов
        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbar)
        val addBtn = view.findViewById<Button>(R.id.add_btn)
        val newEmailTextView = view.findViewById<TextView>(R.id.new_email_edit)
        val nowEmailTextView = view.findViewById<TextView>(R.id.cur_email_edit)

        // Отображение текущей почты
        nowEmailTextView.text = mainActivity.user.email

        // Обработка кнопки "Сохранить"
        addBtn.setOnClickListener {
            if (newEmailTextView.text.toString().isNotEmpty()) {
                changeEmail(mainActivity.uwastingApi, newEmailTextView.text.toString())
            } else {
                val text = getString(R.string.field_is_empty)
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
     * Выполняет запрос на изменение электронной почты через [UWastingApi].
     *
     * В случае успеха обновляет email пользователя и переходит на главный экран.
     * В случае ошибки отображает сообщение о том, что почта уже занята.
     *
     * @param uwastingApi API-интерфейс сервера.
     * @param login Новый email пользователя.
     */
    private fun changeEmail(uwastingApi: UWastingApi, login: String) {
        val mainActivity = activity as MainActivity
        uwastingApi.let {
            compositeDisposable.add(
                uwastingApi.changeLogin(mainActivity.user.id, login)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        // Успех — обновляем локальную модель и переходим к основному экрану
                        mainActivity.user.email = login
                        mainActivity.setFragment(TabFragment())
                    }, {
                        // Ошибка — email уже используется
                        val text = getString(R.string.email_is_used)
                        Toast.makeText(mainActivity, text, Toast.LENGTH_LONG).show()
                    })
            )
        }
    }
}

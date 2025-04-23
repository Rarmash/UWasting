package com.example.uwasting.fragments

import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.uwasting.R
import com.example.uwasting.activities.MainActivity
import com.example.uwasting.activities.StartingActivity
import com.example.uwasting.data.User
import com.example.uwasting.data.remote.UWastingApi
import com.example.uwasting.preferences.MyPreference
import com.google.android.material.appbar.MaterialToolbar
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

/**
 * Фрагмент входа в приложение (авторизация).
 *
 * Пользователь вводит email и пароль. При успешной авторизации данные пользователя сохраняются
 * в [MyPreference], и происходит переход в основное окно приложения ([MainActivity]).
 *
 * Используется в [StartingActivity].
 */
class SignInFragment : Fragment() {

    /** Коллекция подписок RxJava для сетевых операций */
    private val compositeDisposable = CompositeDisposable()

    /**
     * Создание интерфейса фрагмента и настройка обработки событий.
     *
     * @return View фрагмента.
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_sign_in, container, false)
        val startingActivity = activity as StartingActivity

        // Элементы интерфейса
        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbar)
        val passwordEdit = view.findViewById<EditText>(R.id.pswrd_edit)
        val emailEdit = view.findViewById<EditText>(R.id.email_edit)
        val signInBtn = view.findViewById<Button>(R.id.sign_in_btn)

        // Обработка входа в систему
        signInBtn.setOnClickListener {
            if (passwordEdit.text.toString().isBlank() || emailEdit.text.toString().isBlank()) {
                Toast.makeText(startingActivity, getString(R.string.field_is_empty), Toast.LENGTH_LONG).show()
            } else {
                startingActivity.user.password = passwordEdit.text.toString()
                startingActivity.user.email = emailEdit.text.toString()
                tryGet(startingActivity.uwastingApi, emailEdit.text.toString(), passwordEdit.text.toString())
            }
        }

        // Кнопка "Назад"
        toolbar.setNavigationOnClickListener {
            startingActivity.prevFragment()
        }

        return view
    }

    /**
     * Попытка получить пользователя по введённым логину и паролю через API.
     * При успехе сохраняет пользователя и запускает [MainActivity].
     *
     * @param uwastingApi Экземпляр API-клиента.
     * @param email Электронная почта пользователя.
     * @param password Пароль пользователя.
     */
    private fun tryGet(uwastingApi: UWastingApi?, email: String, password: String) {
        val startingActivity = activity as StartingActivity
        uwastingApi?.let {
            compositeDisposable.add(
                uwastingApi.getUserData(email, password)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ user ->
                        // Сохраняем пользователя
                        startingActivity.myPreference.setUser(user)

                        // Переход в главное окно приложения
                        startActivity(Intent(startingActivity, MainActivity::class.java))
                        startingActivity.finish()
                    }, {
                        Toast.makeText(startingActivity, getString(R.string.user_not_found), Toast.LENGTH_LONG).show()
                    })
            )
        }
    }
}

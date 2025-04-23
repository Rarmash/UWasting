package com.example.uwasting.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.uwasting.R
import com.example.uwasting.activities.MainActivity
import com.example.uwasting.activities.StartingActivity
import com.example.uwasting.data.User
import com.example.uwasting.data.remote.UWastingApi
import com.google.android.material.appbar.MaterialToolbar
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

/**
 * Фрагмент для завершения регистрации пользователя: ввод и подтверждение пароля.
 *
 * Выполняется валидация совпадения паролей и регистрация пользователя через [UWastingApi].
 * После успешной регистрации происходит переход в основное окно приложения ([MainActivity]).
 */
class PasswordFragment : Fragment() {

    /** Коллекция подписок RxJava для сетевых запросов */
    private val compositeDisposable = CompositeDisposable()

    /**
     * Создание и отображение представления фрагмента.
     *
     * Настраивает обработчики событий: кнопки "Далее" и "Назад", а также валидацию полей ввода.
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_password, container, false)
        val startingActivity = activity as StartingActivity

        val passwordEdit = view.findViewById<EditText>(R.id.enter_pswrd_edit)      // Ввод пароля
        val repPasswordEdit = view.findViewById<EditText>(R.id.repeat_pswrd_edit)  // Подтверждение пароля
        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbar)
        val nextBtn = view.findViewById<Button>(R.id.next_btn)

        // Обработка нажатия на кнопку "Далее"
        nextBtn.setOnClickListener {
            when {
                passwordEdit.text.toString().isBlank() || repPasswordEdit.text.toString().isBlank() -> {
                    Toast.makeText(startingActivity, getString(R.string.field_is_empty), Toast.LENGTH_LONG).show()
                }
                passwordEdit.text.toString() != repPasswordEdit.text.toString() -> {
                    Toast.makeText(startingActivity, getString(R.string.passwords_dont_match), Toast.LENGTH_LONG).show()
                }
                else -> {
                    startingActivity.user.password = passwordEdit.text.toString()
                    tryRegisterUser(startingActivity.uwastingApi, startingActivity.user)
                }
            }
        }

        // Обработка кнопки "Назад"
        toolbar.setNavigationOnClickListener {
            startingActivity.prevFragment()
        }

        return view
    }

    /**
     * Выполняет попытку регистрации пользователя через API.
     * В случае успеха — сохраняет пользователя в [StartingActivity.myPreference] и запускает [MainActivity].
     *
     * @param uwastingApi API-интерфейс для регистрации.
     * @param user Объект пользователя, содержащий email, пароль, имя и фамилию.
     */
    private fun tryRegisterUser(uwastingApi: UWastingApi, user: User) {
        val startingActivity = activity as StartingActivity

        compositeDisposable.add(
            uwastingApi.registrateUser(user.email, user.password, user.name, user.surname)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ registeredUser ->
                    startingActivity.myPreference.setUser(registeredUser)
                    startActivity(Intent(startingActivity, MainActivity::class.java))
                }, {
                    Toast.makeText(startingActivity, getString(R.string.registration_error), Toast.LENGTH_LONG).show()
                })
        )
    }
}

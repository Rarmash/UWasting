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
import com.example.uwasting.data.Constants
import com.example.uwasting.data.remote.UWastingApi
import com.google.android.material.appbar.MaterialToolbar
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

/**
 * Фрагмент, предназначенный для проверки текущего пароля пользователя перед выполнением чувствительных действий.
 *
 * Используется перед:
 * - сменой электронной почты ([ChangeEmailFragment])
 * - сменой пароля ([ChangePasswordFragment])
 *
 * В случае успешной проверки происходит переход на соответствующий фрагмент, в зависимости от значения [mode].
 *
 * @param mode Режим выполнения действия:
 * - [Constants.CHANGE_EMAIl] — смена почты
 * - [Constants.CHANGE_PASSWORD] — смена пароля
 */
class VerifyPasswordFragment(private var mode: Int) : Fragment() {

    /** Коллекция подписок для асинхронных операций */
    private val compositeDisposable = CompositeDisposable()

    /**
     * Создание и отображение фрагмента.
     * Настраивает валидацию введённого пароля и переход к следующему действию.
     *
     * @return View фрагмента.
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_verify_password, container, false)
        val mainActivity = activity as MainActivity

        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbar)
        val nextBtn = view.findViewById<Button>(R.id.next_btn)
        val passwordTextView = view.findViewById<TextView>(R.id.pswrd_edit)

        // Проверка пароля при нажатии на кнопку "Далее"
        nextBtn.setOnClickListener {
            tryLogin(mainActivity.uwastingApi, mainActivity.user.email, passwordTextView.text.toString())
        }

        // Назад
        toolbar.setNavigationOnClickListener {
            mainActivity.prevFragment()
        }

        return view
    }

    /**
     * Выполняет проверку пароля пользователя через API.
     * При успехе переключает фрагмент согласно выбранному [mode].
     *
     * @param uwastingApi API клиента приложения.
     * @param login Электронная почта текущего пользователя.
     * @param password Введённый пользователем пароль.
     */
    private fun tryLogin(uwastingApi: UWastingApi, login: String, password: String) {
        val mainActivity = activity as MainActivity
        compositeDisposable.add(
            uwastingApi.getUserData(login, password)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    when (mode) {
                        Constants.CHANGE_EMAIl -> mainActivity.setFragment(ChangeEmailFragment())
                        Constants.CHANGE_PASSWORD -> mainActivity.setFragment(ChangePasswordFragment())
                    }
                }, {
                    Toast.makeText(mainActivity, getString(R.string.passwords_dont_match), Toast.LENGTH_LONG).show()
                })
        )
    }
}

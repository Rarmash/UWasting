package com.example.uwasting.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.uwasting.R
import com.example.uwasting.activities.StartingActivity
import com.example.uwasting.data.remote.UWastingApi
import com.google.android.material.appbar.MaterialToolbar
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

/**
 * Фрагмент ввода электронной почты при регистрации.
 *
 * Отображает поле ввода email, кнопку "Далее" и выполняет проверку,
 * свободна ли почта в базе данных через [UWastingApi].
 * При успешной проверке происходит переход к экрану ввода имени и фамилии ([NameFragment]).
 */
class EmailFragment : Fragment() {

    /** Коллекция подписок RxJava для управления сетевыми запросами */
    private val compositeDisposable = CompositeDisposable()

    /**
     * Создаёт и возвращает представление фрагмента.
     *
     * Инициализирует элементы интерфейса: поле ввода email, кнопку "Далее" и обработку возврата назад.
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_email, container, false)
        val startingActivity = activity as StartingActivity

        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbar)
        val nextBtn = view.findViewById<Button>(R.id.next_btn)
        val emailEdit = view.findViewById<EditText>(R.id.email_edit)

        // Обработка нажатия кнопки "Далее"
        nextBtn.setOnClickListener {
            if (emailEdit.text.toString().isBlank()) {
                val text = getString(R.string.field_is_empty)
                Toast.makeText(startingActivity, text, Toast.LENGTH_LONG).show()
            } else {
                startingActivity.user.email = emailEdit.text.toString()
                checkMail(startingActivity.uwastingApi, startingActivity.user.email)
            }
        }

        // Обработка кнопки "Назад"
        toolbar.setNavigationOnClickListener {
            startingActivity.prevFragment()
        }

        return view
    }

    /**
     * Выполняет проверку email на наличие в базе данных.
     *
     * Если email занят, выводится сообщение. Иначе происходит переход на фрагмент [NameFragment].
     *
     * @param uwastingApi Интерфейс API приложения.
     * @param email Введённая пользователем почта.
     */
    private fun checkMail(uwastingApi: UWastingApi?, email: String) {
        val startingActivity = activity as StartingActivity

        uwastingApi?.let {
            compositeDisposable.add(
                it.checkEmail(email)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ exists ->
                        if (exists) {
                            val text = getString(R.string.email_is_used)
                            Toast.makeText(startingActivity, text, Toast.LENGTH_LONG).show()
                        } else {
                            startingActivity.setFragment(NameFragment())
                        }
                    }, {
                        // Обработка ошибки (можно добавить лог или сообщение)
                    })
            )
        }
    }
}

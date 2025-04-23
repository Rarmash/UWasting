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
 * Фрагмент экрана аккаунта.
 *
 * Позволяет пользователю изменить имя и фамилию, отправив изменения на сервер.
 * Содержит поле ввода, кнопку "Сохранить изменения" и кнопку возврата в тулбаре.
 */
class AccountFragment : Fragment() {

    /** Коллекция подписок для RxJava */
    private val compositeDisposable = CompositeDisposable()

    /**
     * Пытается изменить имя и фамилию пользователя на сервере.
     * В случае успеха возвращает пользователя на предыдущий экран.
     * В случае ошибки отображает сообщение.
     *
     * @param uwastingApi Интерфейс взаимодействия с сервером.
     * @param id ID пользователя.
     * @param name Новое имя.
     * @param surname Новая фамилия.
     */
    private fun tryChange(uwastingApi: UWastingApi?, id: Int, name: String, surname: String) {
        val mainActivity = activity as MainActivity
        uwastingApi?.let {
            compositeDisposable.add(
                uwastingApi.changeNameSurname(id, name, surname)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        mainActivity.user.name = name
                        mainActivity.user.surname = surname
                        mainActivity.prevFragment()
                    }, {
                        val text = getString(R.string.name_error)
                        val toast = Toast.makeText(mainActivity, text, Toast.LENGTH_LONG)
                        toast.show()
                    })
            )
        }
    }

    /**
     * Создаёт и возвращает представление фрагмента.
     *
     * Настраивает:
     * - поля ввода имени и фамилии;
     * - кнопку "Сохранить изменения";
     * - кнопку возврата в тулбаре.
     *
     * @return Представление фрагмента.
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_account, container, false)
        val mainActivity = activity as MainActivity

        val saveChangesBtn = view.findViewById<Button>(R.id.add_btn)
        val nameTextView = view.findViewById<TextView>(R.id.name_edit)
        val surnameTextView = view.findViewById<TextView>(R.id.surname_edit)

        // Обработка нажатия кнопки "Сохранить"
        saveChangesBtn.setOnClickListener {
            if (nameTextView.text.toString().isEmpty() || surnameTextView.text.toString().isEmpty()) {
                val text = getString(R.string.field_is_empty)
                val toast = Toast.makeText(mainActivity, text, Toast.LENGTH_LONG)
                toast.show()
            } else {
                tryChange(
                    mainActivity.uwastingApi,
                    mainActivity.user.id,
                    nameTextView.text.toString(),
                    surnameTextView.text.toString()
                )
            }
        }

        // Обработка кнопки "Назад" в тулбаре
        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener {
            mainActivity.prevFragment()
        }

        return view
    }
}

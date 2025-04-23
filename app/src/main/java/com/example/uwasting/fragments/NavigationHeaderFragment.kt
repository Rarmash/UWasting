package com.example.uwasting.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.example.uwasting.R
import com.example.uwasting.activities.MainActivity

/**
 * Фрагмент, отображающий заголовок бокового меню (Navigation Drawer).
 *
 * При нажатии на заголовок выполняется переход к экрану профиля ([AccountFragment]).
 * Обычно используется в макете `NavigationView` как заголовок.
 */
class NavigationHeaderFragment : Fragment() {

    /**
     * Создаёт и возвращает представление фрагмента.
     *
     * Настраивает обработку клика по заголовку, ведущую на экран редактирования профиля.
     *
     * @return Представление фрагмента.
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_navigation_header, container, false)
        val mainActivity = activity as MainActivity

        // Корневой контейнер заголовка
        val layout = view.findViewById<LinearLayout>(R.id.layout)

        // При нажатии открываем фрагмент аккаунта
        layout.setOnClickListener {
            mainActivity.setFragment(AccountFragment())
        }

        return view
    }
}

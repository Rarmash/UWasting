@file:Suppress("NAME_SHADOWING")

package com.example.uwasting.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.uwasting.R
import com.example.uwasting.activities.MainActivity
import com.example.uwasting.activities.StartingActivity
import com.example.uwasting.adapters.ViewPagerAdapter
import com.example.uwasting.data.Constants
import com.example.uwasting.data.OnBackButtonListener
import com.example.uwasting.data.User
import com.example.uwasting.dialogs.LanguageDialog
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import androidx.appcompat.app.AppCompatDelegate

/**
 * Главный фрагмент после авторизации, отображающий две вкладки:
 * - Доходы ([IncomesFragment])
 * - Расходы ([ExpensesFragment])
 *
 * Также реализует навигационное меню с возможностью:
 * - Изменить почту или пароль
 * - Сменить язык или валюту
 * - Выйти из аккаунта
 * - Перейти в профиль пользователя
 *
 * Используется внутри [MainActivity].
 */
class TabFragment : Fragment(), OnBackButtonListener {

    /**
     * Отображает фрагмент, настраивает ViewPager, TabLayout и Navigation Drawer.
     *
     * @return View интерфейса вкладочного фрагмента.
     */
    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_tab, container, false)
        val mainActivity = activity as MainActivity

        // Инициализация элементов
        val viewPager = view.findViewById<ViewPager2>(R.id.view_pager)
        val tabLayout = view.findViewById<TabLayout>(R.id.tab_layout)
        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbar)
        val drawerLayout = view.findViewById<DrawerLayout>(R.id.drawerLayout)
        val navigationView = view.findViewById<NavigationView>(R.id.navigation_view)
        val nameTxt = navigationView.getHeaderView(0).findViewById<TextView>(R.id.name_txt)
        val emailTxt = navigationView.getHeaderView(0).findViewById<TextView>(R.id.email_txt)

        // Открытие бокового меню
        toolbar.setNavigationOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        // Установка адаптера для ViewPager
        val viewPagerAdapter = ViewPagerAdapter(this)
        viewPager.adapter = viewPagerAdapter

        // Настройка заголовков вкладок
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> getString(R.string.incomes)
                1 -> getString(R.string.expenses)
                else -> ""
            }
        }.attach()

        // Данные пользователя
        nameTxt.text = "${mainActivity.user.name} ${mainActivity.user.surname}"
        emailTxt.text = mainActivity.user.email

        // Обработка нажатий в боковом меню
        navigationView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.change_email -> mainActivity.setFragment(VerifyPasswordFragment(Constants.CHANGE_EMAIl))
                R.id.change_password -> mainActivity.setFragment(VerifyPasswordFragment(Constants.CHANGE_PASSWORD))
                R.id.sign_out -> {
                    mainActivity.myPreference.setUser(User().apply {
                        id = -1
                        name = ""
                        surname = ""
                        email = ""
                    })
                    startActivity(Intent(mainActivity, StartingActivity::class.java))
                    mainActivity.finish()
                }
                R.id.currency -> {
                    mainActivity.curr = "₽"
                    mainActivity.ue = 1
                    viewPager.adapter = ViewPagerAdapter(this)
                }
                R.id.theme_toggle -> {
                    val currentTheme = mainActivity.myPreference.getTheme()
                    if (currentTheme == "light") {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                        mainActivity.myPreference.setTheme("dark")
                    } else {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                        mainActivity.myPreference.setTheme("light")
                    }
                    // Перезапускаем активити, чтобы тема применились
                    mainActivity.recreate()
                }
                R.id.language -> {
                    LanguageDialog().show(parentFragmentManager, "language")
                }
            }
            true
        }

        // Переход в аккаунт по нажатию на хедер
        navigationView.getHeaderView(0).setOnClickListener {
            mainActivity.setFragment(AccountFragment())
        }

        return view
    }

    /**
     * Обработка кнопки "Назад" — фрагмент не закрывается.
     */
    override fun onBackPressed(): Boolean {
        return true
    }
}

package com.example.uwasting.adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.uwasting.fragments.ExpensesFragment
import com.example.uwasting.fragments.IncomesFragment

/**
 * Адаптер для ViewPager2, используемый для переключения между вкладками "Доходы" и "Расходы".
 *
 * @param fragment Родительский фрагмент, в котором расположен ViewPager2.
 */
class ViewPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    /** Общее количество страниц (вкладок): Доходы и Расходы */
    private val numPages = 2

    /**
     * Возвращает фрагмент, соответствующий указанной позиции.
     *
     * @param position Индекс вкладки:
     * - `0` — фрагмент с доходами [IncomesFragment]
     * - `1` — фрагмент с расходами [ExpensesFragment]
     * @return Соответствующий фрагмент.
     */
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> IncomesFragment()
            1 -> ExpensesFragment()
            else -> IncomesFragment() // по умолчанию
        }
    }

    /**
     * Возвращает количество вкладок.
     *
     * @return Общее число страниц (2).
     */
    override fun getItemCount(): Int {
        return numPages
    }
}

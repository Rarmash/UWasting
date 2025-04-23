package com.example.uwasting.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.uwasting.R
import com.example.uwasting.activities.MainActivity
import com.example.uwasting.data.Categories
import com.example.uwasting.data.Constants
import com.example.uwasting.data.SelectingCategoryRecyclerView
import com.google.android.material.appbar.MaterialToolbar

/**
 * Фрагмент для выбора категории дохода или расхода.
 *
 * Отображает список категорий в зависимости от переданного типа (`INCOMES` или `EXPENSES`),
 * позволяет пользователю выбрать одну категорию и передаёт результат во фрагмент-источник через интерфейс [SetCategory].
 *
 * Используется в фрагментах [NewIncomeFragment] и [NewExpenseFragment].
 *
 * @param setCategory интерфейс обратного вызова, в который передаётся выбранная категория.
 * @param categoriesType тип категорий: [Constants.INCOMES] или [Constants.EXPENSES].
 */
class SelectCategoryFragment(
    private var setCategory: SetCategory,
    private var categoriesType: Int
) : Fragment() {

    /**
     * Создание и отображение фрагмента.
     * Настраивает список категорий, обработку выбора и возврат на предыдущий экран.
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_select_category, container, false)
        val mainActivity = activity as MainActivity

        // Виджеты
        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbar)
        val categoriesList = view.findViewById<RecyclerView>(R.id.categories_list)
        val selectBtn = view.findViewById<Button>(R.id.select_btn)

        // Адаптер списка категорий
        val adapter = SelectingCategoryRecyclerView()
        categoriesList.adapter = adapter
        categoriesList.layoutManager = LinearLayoutManager(mainActivity)

        // Установка категорий в список
        val categories = Categories()
        when (categoriesType) {
            Constants.EXPENSES -> categories.expenses.forEach { adapter.addItem(it) }
            Constants.INCOMES -> categories.incomes.forEach { adapter.addItem(it) }
        }

        // Обработка выбора категории
        selectBtn.setOnClickListener {
            setCategory.setCategory(adapter.selectedCategoryName)
            mainActivity.prevFragment()
        }

        // Обработка кнопки "Назад"
        toolbar.setNavigationOnClickListener {
            mainActivity.prevFragment()
        }

        return view
    }
}

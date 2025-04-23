package com.example.uwasting.data

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.uwasting.R

/**
 * Адаптер для [RecyclerView], используемый при выборе категории операции.
 * Позволяет пользователю выбрать одну из доступных категорий с помощью радиокнопки.
 */
class SelectingCategoryRecyclerView : RecyclerView.Adapter<SelectingCategoryRecyclerView.SelectingCategoryViewHolder>() {

    /** Список доступных категорий для выбора */
    private val categoriesList: ArrayList<Category> = ArrayList()

    /** Последний выбранный элемент (ViewHolder), используется для сброса предыдущего выбора */
    private var selectedCategory: SelectingCategoryViewHolder? = null

    /** Название выбранной категории */
    var selectedCategoryName = ""

    /**
     * ViewHolder для отображения одной категории с названием, иконкой и радиокнопкой.
     */
    class SelectingCategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        /** Иконка категории */
        var categoryImg: ImageView = itemView.findViewById(R.id.category_img)

        /** Название категории */
        var nameTxt: TextView = itemView.findViewById(R.id.name_txt)

        /** Радиокнопка для выбора категории */
        var selectBtn: RadioButton = itemView.findViewById(R.id.selectBtn)
    }

    /**
     * Создаёт новый [ViewHolder] на основе макета `view_selecting_category`.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectingCategoryViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.view_selecting_category, parent, false)
        return SelectingCategoryViewHolder(itemView)
    }

    /**
     * Отображает данные категории и настраивает обработку выбора радиокнопки.
     */
    override fun onBindViewHolder(holder: SelectingCategoryViewHolder, position: Int) {
        val category = categoriesList[position]

        holder.nameTxt.text = category.name
        holder.categoryImg.setImageResource(category.srcImage)

        holder.selectBtn.setOnClickListener {
            selectedCategory?.selectBtn?.isChecked = false
            selectedCategory = holder
            selectedCategoryName = category.name
        }
    }

    /**
     * Возвращает общее количество категорий в списке.
     */
    override fun getItemCount(): Int {
        return categoriesList.size
    }

    /**
     * Добавляет новую категорию в список и обновляет отображение.
     *
     * @param category Категория, которую нужно добавить.
     */
    fun addItem(category: Category) {
        categoriesList.add(category)
        notifyDataSetChanged()
    }
}

package com.example.uwasting.data

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.uwasting.R
import com.example.uwasting.activities.MainActivity
import kotlin.math.round

/**
 * Интерфейс обработчика нажатий на элементы списка категорий.
 */
interface OnItemClickListener {
    /**
     * Вызывается при клике на элемент списка.
     *
     * @param item Тройка, содержащая:
     * - [Category] — категория
     * - [Int] — количество операций
     * - [Int] — сумма по категории
     */
    fun onItemClicked(item: Triple<Category, Int, Int>)
}

/**
 * Адаптер для отображения списка категорий с информацией об операциях.
 *
 * Используется для вывода категории, количества операций и суммы по каждой из них в [RecyclerView].
 *
 * @param data Список троек: категория, количество операций, сумма.
 * @param itemClickListener Обработчик нажатий на элемент.
 * @param mainActivity Ссылка на активность, из которой вызывается адаптер (для доступа к валюте и курсу).
 */
class CategoryRecyclerView(
    private val data: ArrayList<Triple<Category, Int, Int>>,
    private val itemClickListener: OnItemClickListener,
    private var mainActivity: MainActivity
) : RecyclerView.Adapter<CategoryRecyclerView.CategoryViewHolder>() {

    /**
     * ViewHolder для отображения одного элемента категории.
     *
     * @param itemView Представление (View), соответствующее элементу списка.
     */
    class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTxt: TextView = itemView.findViewById(R.id.name_txt)
        val operationsAmountTxt: TextView = itemView.findViewById(R.id.operations_txt)
        val sumTxt: TextView = itemView.findViewById(R.id.sum_txt)
        val categoryImg: ImageView = itemView.findViewById(R.id.category_img)

        /**
         * Привязывает обработчик нажатия к элементу списка.
         *
         * @param item Элемент списка.
         * @param clickListener Объект, реализующий [OnItemClickListener].
         */
        fun bind(item: Triple<Category, Int, Int>, clickListener: OnItemClickListener) {
            itemView.setOnClickListener {
                clickListener.onItemClicked(item)
            }
        }
    }

    /**
     * Создаёт новый ViewHolder.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.view_category, parent, false)
        return CategoryViewHolder(itemView)
    }

    /**
     * Привязывает данные к ViewHolder.
     */
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val item = data[position]
        val category = item.first
        val operationCount = item.second
        val sum = item.third

        holder.nameTxt.text = category.name
        holder.operationsAmountTxt.text = "Всего операций: $operationCount"

        val sumValue = round(sum.toFloat() / mainActivity.ue * 100) / 100.0
        holder.sumTxt.text = if (sum > 0) "+$sumValue${mainActivity.curr}" else "$sumValue${mainActivity.curr}"

        holder.categoryImg.setImageResource(category.srcImage)

        holder.bind(item, itemClickListener)
    }

    /**
     * Возвращает общее количество элементов в списке.
     */
    override fun getItemCount(): Int {
        return data.size
    }
}

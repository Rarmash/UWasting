package com.example.uwasting.data

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.uwasting.R
import com.example.uwasting.activities.MainActivity
import java.time.LocalDate
import kotlin.math.round

/**
 * Интерфейс обработчика нажатий на элементы списка операций.
 */
interface OnOperationClickListener {

    /**
     * Метод, вызываемый при клике на операцию в списке.
     *
     * @param item Тройка, содержащая:
     * - [LocalDate] — дата операции,
     * - [Category] — категория операции,
     * - [Int] — сумма операции.
     */
    fun onItemClick(item: Triple<LocalDate, Category, Int>)
}

/**
 * Адаптер для отображения списка операций в [RecyclerView].
 * Используется на главном экране приложения для визуализации истории расходов и доходов.
 *
 * @param data Список троек (дата, категория, сумма).
 * @param onOperationClickListener Обработчик нажатий на элемент списка.
 * @param mainActivity Ссылка на активность, используемая для доступа к валюте и коэффициенту перевода.
 */
class OperationsRecyclerView(
    private val data: ArrayList<Triple<LocalDate, Category, Int>>,
    private var onOperationClickListener: OnOperationClickListener,
    private var mainActivity: MainActivity
) : RecyclerView.Adapter<OperationsRecyclerView.OperationViewHolder>() {

    /**
     * ViewHolder, отображающий одну операцию.
     *
     * @param itemView Представление элемента списка.
     */
    class OperationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var dateTxt: TextView = itemView.findViewById(R.id.date_txt)
        var nameTxt: TextView = itemView.findViewById(R.id.name_txt)
        var sumTxt: TextView = itemView.findViewById(R.id.sum_txt)

        /**
         * Привязывает обработчик нажатий к элементу.
         *
         * @param item Тройка (дата, категория, сумма).
         * @param onOperationClickListener Обработчик кликов.
         */
        fun bind(item: Triple<LocalDate, Category, Int>, onOperationClickListener: OnOperationClickListener) {
            itemView.setOnClickListener {
                onOperationClickListener.onItemClick(item)
            }
        }
    }

    /**
     * Создаёт новый [ViewHolder] для элемента списка.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OperationViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.view_operation, parent, false)
        return OperationViewHolder(itemView)
    }

    /**
     * Привязывает данные операции к [ViewHolder].
     */
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: OperationViewHolder, position: Int) {
        val item = data[position]

        holder.dateTxt.text = item.first.toString()
        holder.nameTxt.text = item.second.name

        val amountFormatted = round(item.third.toFloat() / mainActivity.ue * 100) / 100.0
        holder.sumTxt.text = if (item.third > 0) "+$amountFormatted${mainActivity.curr}" else "$amountFormatted${mainActivity.curr}"

        holder.bind(item, onOperationClickListener)
    }

    /**
     * Возвращает общее количество элементов в списке.
     */
    override fun getItemCount(): Int = data.size
}

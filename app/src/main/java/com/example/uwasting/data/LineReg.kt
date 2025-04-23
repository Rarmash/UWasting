package com.example.uwasting.data

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import org.nield.kotlinstatistics.*
import java.sql.Date
import java.time.LocalDate
import java.time.LocalDate.now
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.exp

/**
 * Класс, реализующий линейную регрессию для анализа финансовых расходов пользователя.
 *
 * Используется для прогнозирования будущих трат на основе имеющихся данных.
 *
 * @param expenses Список операций пользователя (только расходы), по которым будет построена регрессионная модель.
 */
class LineReg(private var expenses: ArrayList<Operation>) {

    /**
     * Вычисляет прогнозируемую сумму расходов на следующие 30 дней на основе линейной регрессии.
     *
     * Для каждой даты вычисляется общая сумма расходов, затем на основе этих значений строится
     * линейная модель при помощи библиотеки `kotlin-statistics`. После этого осуществляется предсказание
     * значений на каждый из следующих 30 дней.
     *
     * @return Прогнозируемая сумма расходов за 30 дней.
     *
     * Возможные случаи:
     * - Если данных нет — возвращается `0.0`
     * - Если одна точка — прогнозируется как 30 * сумма
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun evaluateAlgorithm(): Double {
        val dateAmount: MutableMap<Long, Int> = mutableMapOf()

        // Группировка операций по дате
        for (i in expenses) {
            val day = i.date.toEpochDay()
            dateAmount[day] = (dateAmount[day] ?: 0) + i.amount
        }

        // Преобразование в список пар (дата, сумма)
        val tmp = ArrayList<Pair<Long, Int>>()
        for (entry in dateAmount) {
            tmp.add(Pair(entry.key, entry.value))
        }

        // Проверка на крайние случаи
        if (tmp.isEmpty()) return 0.0
        if (tmp.size == 1) return tmp[0].second.toDouble() * 30

        // Построение модели линейной регрессии
        val regression = tmp.simpleRegression(
            xSelector = { it.first },
            ySelector = { it.second }
        )

        // Прогноз на следующие 30 дней
        var sum = 0.0
        for (i in 0..30) {
            val futureDay = LocalDate.now().toEpochDay().toDouble() + i
            sum += regression.predict(futureDay)
        }

        return sum
    }
}

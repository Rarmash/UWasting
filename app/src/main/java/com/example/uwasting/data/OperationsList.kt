package com.example.uwasting.data

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*

/**
 * Класс-обёртка над списком операций пользователя.
 * Предоставляет методы фильтрации, агрегации и анализа данных о расходах и доходах.
 *
 * @property list Список операций, переданных в конструкторе.
 */
class OperationsList(var item: ArrayList<Operation>) {
    var list = item

    /**
     * Возвращает список всех операций, представляющих доходы.
     */
    fun selectOperationsIncomes(): ArrayList<Operation> {
        val tmp = ArrayList<Operation>()
        for (i in list) {
            if (i.amount > 0) tmp.add(i)
        }
        return tmp
    }

    /**
     * Возвращает список всех операций, представляющих расходы.
     */
    fun selectOperationsExpenses(): ArrayList<Operation> {
        val tmp = ArrayList<Operation>()
        for (i in list) {
            if (i.amount < 0) tmp.add(i)
        }
        return tmp
    }

    /**
     * Возвращает общую сумму всех доходов.
     */
    fun getTotalSumIncomes(): Int {
        var tmp = 0
        for (i in list) {
            if (i.amount > 0) tmp += i.amount
        }
        return tmp
    }

    /**
     * Возвращает общую сумму всех расходов.
     */
    fun getTotalSumExpenses(): Int {
        var tmp = 0
        for (i in list) {
            if (i.amount < 0) tmp += i.amount
        }
        return tmp
    }

    /**
     * Группирует доходы по категориям, возвращая список троек:
     * (Категория, Количество операций, Общая сумма).
     */
    fun combineByCategoryIncomes(): ArrayList<Triple<Category, Int, Int>> {
        val res = ArrayList<Triple<Category, Int, Int>>()
        val tmp: MutableMap<String, Pair<Int, Int>> = mutableMapOf()
        for (i in list) {
            if (i.amount > 0) {
                tmp[i.category] = tmp.getOrDefault(i.category, Pair(0, 0))
                    .let { Pair(it.first + 1, it.second + i.amount) }
            }
        }
        val categories = Categories()
        for (i in tmp) {
            res.add(Triple(categories.hasInCommon(i.key), i.value.first, i.value.second))
        }
        return ArrayList(res.sortedBy { it.third }.reversed())
    }

    /**
     * Группирует расходы по категориям, возвращая список троек:
     * (Категория, Количество операций, Общая сумма).
     */
    fun combineByCategoryExpenses(): ArrayList<Triple<Category, Int, Int>> {
        val res = ArrayList<Triple<Category, Int, Int>>()
        val tmp: MutableMap<String, Pair<Int, Int>> = mutableMapOf()
        for (i in list) {
            if (i.amount < 0) {
                tmp[i.category] = tmp.getOrDefault(i.category, Pair(0, 0))
                    .let { Pair(it.first + 1, it.second + i.amount) }
            }
        }
        val categories = Categories()
        for (i in tmp) {
            res.add(Triple(categories.hasInCommon(i.key), i.value.first, i.value.second))
        }
        return ArrayList(res.sortedBy { it.third })
    }

    /**
     * Сортирует операции по дате в порядке убывания.
     *
     * @return Список троек: (дата, категория, сумма).
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun sortByDate(): ArrayList<Triple<LocalDate, Category, Int>> {
        val res = ArrayList<Triple<LocalDate, Category, Int>>()
        val categories = Categories()

        for (i in list) {
            res.add(Triple(i.date, categories.hasInCommon(i.category), i.amount))
        }

        return ArrayList(res.sortedBy { it.first }.reversed())
    }

    /**
     * Отбирает операции за заданный период (в днях от текущей даты).
     *
     * @param Period Количество дней назад, начиная от текущей даты.
     * @return Список операций за этот период.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun selectOperations(Period: Int): List<Operation> {
        val now = LocalDate.now()
        val tmp = ArrayList<Operation>()
        for (i in list) {
            if (now.minusDays(Period.toLong()) < i.date) tmp.add(i)
        }
        return tmp
    }

    /**
     * Отбирает все операции, относящиеся к указанной категории.
     *
     * @param category Категория, по которой нужно фильтровать.
     * @return Новый экземпляр [OperationsList] с отобранными операциями.
     */
    fun selectByCategory(category: Category): OperationsList {
        val tmp = ArrayList<Operation>()
        for (i in list) {
            if (i.category == category.name) tmp.add(i)
        }
        return OperationsList(tmp)
    }

    /**
     * Объединяет операции по дате, считая отдельно сумму доходов и расходов для каждой даты.
     *
     * @return Словарь: ключ — дата, значение — пара (доходы, расходы).
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun combineByDateIncomesAndExpenses(): MutableMap<LocalDate, Pair<Int, Int>> {
        val tmp: MutableMap<LocalDate, Pair<Int, Int>> = mutableMapOf()
        val sortedList = OperationsList(list).sortByDate().reversed()
        for (i in sortedList) {
            tmp[i.first] = tmp.getOrDefault(i.first, Pair(0, 0)).let { pair ->
                if (i.third < 0) Pair(pair.first, pair.second + i.third)
                else Pair(pair.first + i.third, pair.second)
            }
        }
        return tmp
    }

    /**
     * Ищет ID операции по её параметрам.
     *
     * @param date Дата операции.
     * @param amount Сумма операции.
     * @param category Категория операции.
     * @return ID найденной операции или -1, если не найдена.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun findOperation(date: LocalDate, amount: Int, category: Category): Int {
        for (i in list) {
            if (i.date == date && i.amount == amount && i.category == category.name) {
                return i.id
            }
        }
        return -1
    }

    /**
     * Удаляет операцию из списка по её ID.
     *
     * @param id Уникальный идентификатор операции.
     */
    fun removeOperation(id: Int) {
        for (i in list) {
            if (i.id == id) {
                list.remove(i)
                break
            }
        }
    }

    /**
     * Добавляет новую операцию в список.
     *
     * @param amount Сумма операции.
     * @param category Название категории.
     * @param date Дата операции.
     * @param id Уникальный идентификатор.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun addOperation(amount: Int, category: String, date: LocalDate, id: Int) {
        list.add(Operation(amount, category, date, id))
    }
}

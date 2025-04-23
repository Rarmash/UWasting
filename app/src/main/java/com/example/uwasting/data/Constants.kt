package com.example.uwasting.data

/**
 * Класс, содержащий глобальные константы, используемые в приложении UWasting.
 * Константы сгруппированы в объект-компаньон [companion object] для удобного доступа.
 */
class Constants {
    companion object {
        /** Код действия: смена электронной почты */
        const val CHANGE_EMAIl = 0

        /** Код действия: смена пароля */
        const val CHANGE_PASSWORD = 1

        /** Базовый URL-адрес серверного API приложения UWasting */
        const val APIUrl = "https://uwasting.herokuapp.com/"

        /** URL-адрес внешнего API сервиса StatBureau */
        const val StatBureauUrl = "https://www.statbureau.org/"

        /** Тип операции: расходы */
        const val EXPENSES = 0

        /** Тип операции: доходы */
        const val INCOMES = 1
    }
}

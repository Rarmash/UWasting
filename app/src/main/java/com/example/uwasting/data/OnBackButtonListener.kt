package com.example.uwasting.data

/**
 * Интерфейс для обработки пользовательских действий при нажатии кнопки "Назад" внутри фрагментов.
 *
 * Если фрагмент реализует этот интерфейс, его метод [onBackPressed] будет вызван при нажатии кнопки "Назад".
 * Это позволяет перехватывать поведение по умолчанию и выполнять собственную логику (например, показать диалог).
 */
interface OnBackButtonListener {

    /**
     * Метод, вызываемый при нажатии кнопки "Назад".
     *
     * @return `true`, если действие обработано и не требует дальнейших действий;
     * `false`, если нужно продолжить стандартную обработку (например, закрыть фрагмент).
     */
    fun onBackPressed(): Boolean
}

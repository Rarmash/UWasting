package com.example.uwasting.data

/**
 * Модель пользователя приложения UWasting.
 *
 * Используется для хранения и передачи данных авторизованного или зарегистрированного пользователя.
 *
 * @property email Электронная почта пользователя, используемая как логин.
 * @property password Пароль пользователя.
 * @property name Имя пользователя.
 * @property surname Фамилия пользователя.
 * @property id Уникальный идентификатор пользователя в базе данных.
 */
class User {
    /** Электронная почта пользователя */
    lateinit var email: String

    /** Пароль пользователя */
    lateinit var password: String

    /** Имя пользователя */
    lateinit var name: String

    /** Фамилия пользователя */
    lateinit var surname: String

    /** Уникальный идентификатор пользователя */
    var id: Int = 0
}

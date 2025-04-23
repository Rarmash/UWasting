package com.example.uwasting.preferences

import android.content.Context
import com.example.uwasting.data.User

const val PREFERENCE_NAME = "SharedPreferenceExample"
const val PREFERENCE_LANGUAGE = "Language"
const val PREFERENCES_USER_NAME = "Username"
const val PREFERENCES_USER_SURNAME = "UserSurname"
const val PREFERENCES_USER_EMAIL = "UserEmail"
const val PREFERENCES_USER_ID = "UserId"

/**
 * Класс-обёртка над `SharedPreferences`, используемый для хранения пользовательских настроек и данных.
 *
 * Применяется для:
 * - Сохранения и получения текущего языка интерфейса
 * - Локального хранения данных пользователя (безопасно, но не шифруется)
 *
 * Используется во всех частях приложения, где требуется доступ к авторизованному пользователю или настройкам языка.
 *
 * @param context Контекст приложения, передаваемый из активити или фрагмента.
 */
class MyPreference(context: Context) {

    /** Экземпляр SharedPreferences */
    private val preference = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)

    /**
     * Получает текущий язык интерфейса, сохранённый в настройках.
     * @return строка с языковым кодом (например, "en" или "ru")
     */
    fun getLanguage(): String {
        return preference.getString(PREFERENCE_LANGUAGE, "en")!!
    }

    /**
     * Устанавливает язык интерфейса.
     * @param Language строка с языковым кодом (например, "en" или "ru")
     */
    fun setLanguage(Language: String) {
        val editor = preference.edit()
        editor.putString(PREFERENCE_LANGUAGE, Language)
        editor.apply()
    }

    /**
     * Сохраняет данные пользователя в `SharedPreferences`.
     * @param user объект [User], содержащий имя, фамилию, email и ID.
     */
    fun setUser(user: User) {
        val editor = preference.edit()
        editor.putString(PREFERENCES_USER_NAME, user.name)
        editor.putString(PREFERENCES_USER_SURNAME, user.surname)
        editor.putString(PREFERENCES_USER_EMAIL, user.email)
        editor.putInt(PREFERENCES_USER_ID, user.id)
        editor.apply()
    }

    /**
     * Извлекает сохранённые данные пользователя.
     * @return объект [User], построенный из локально сохранённых данных.
     */
    fun getUser(): User {
        val user = User()
        user.name = preference.getString(PREFERENCES_USER_NAME, "")!!
        user.surname = preference.getString(PREFERENCES_USER_SURNAME, "")!!
        user.email = preference.getString(PREFERENCES_USER_EMAIL, "")!!
        user.id = preference.getInt(PREFERENCES_USER_ID, -1)
        return user
    }
}

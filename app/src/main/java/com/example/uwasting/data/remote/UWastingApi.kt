package com.example.uwasting.data.remote

import com.example.uwasting.data.Operation
import com.example.uwasting.data.User
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

/**
 * Интерфейс взаимодействия с серверной частью приложения UWasting.
 * Предоставляет методы для регистрации, авторизации, изменения данных пользователя и работы с операциями.
 */
interface UWastingApi {

    /**
     * Получает данные пользователя по логину и паролю.
     *
     * @param email Электронная почта пользователя.
     * @param password Пароль пользователя.
     * @return [Single] с объектом [User], если авторизация успешна.
     */
    @GET("/GetByLoginAndPassword")
    @Headers("Content-Type: application/json")
    fun getUserData(
        @Query("login") email: String,
        @Query("password") password: String
    ): Single<User>

    /**
     * Проверяет наличие электронной почты в базе данных.
     *
     * @param email Электронная почта.
     * @return [Single] с `true`, если почта найдена, иначе `false`.
     */
    @GET("/FindLoginInDB")
    @Headers("Content-Type: application/json")
    fun checkEmail(@Query("login") email: String): Single<Boolean>

    /**
     * Регистрирует нового пользователя.
     *
     * @param email Электронная почта.
     * @param password Пароль.
     * @param name Имя.
     * @param surname Фамилия.
     * @return [Single] с зарегистрированным объектом [User].
     */
    @GET("/RegistrateUser")
    @Headers("Content-Type: application/json")
    fun registrateUser(
        @Query("login") email: String,
        @Query("password") password: String,
        @Query("name") name: String,
        @Query("surname") surname: String
    ): Single<User>

    /**
     * Изменяет имя и фамилию пользователя.
     *
     * @param id ID пользователя.
     * @param newName Новое имя.
     * @param newSurname Новая фамилия.
     * @return [Single] с `true`, если изменение прошло успешно.
     */
    @GET("/СhangeNameSurname")
    @Headers("Content-Type: application/json")
    fun changeNameSurname(
        @Query("id") id: Int,
        @Query("name") newName: String,
        @Query("surname") newSurname: String
    ): Single<Boolean>

    /**
     * Изменяет логин (email) пользователя.
     *
     * @param id ID пользователя.
     * @param email Новый email.
     * @return [Single] с `true`, если операция успешна.
     */
    @GET("/ChangeLogin")
    @Headers("Content-Type: application/json")
    fun changeLogin(
        @Query("id") id: Int,
        @Query("login") email: String
    ): Single<Boolean>

    /**
     * Изменяет пароль пользователя.
     *
     * @param id ID пользователя.
     * @param password Новый пароль.
     * @return [Single] с `true`, если операция успешна.
     */
    @GET("/ChangePassword")
    @Headers("Content-Type: application/json")
    fun changePassword(
        @Query("id") id: Int,
        @Query("password") password: String
    ): Single<Boolean>

    /**
     * Получает список операций пользователя.
     *
     * @param id ID пользователя.
     * @return [Single] со списком [Operation].
     */
    @GET("/GetOperations")
    @Headers("Content-Type: application/json")
    fun getOperations(@Query("UserId") id: Int): Single<ArrayList<Operation>>

    /**
     * Добавляет новую финансовую операцию.
     *
     * @param value Сумма операции.
     * @param category Категория.
     * @param date Дата (в формате "yyyy-MM-dd").
     * @param id ID пользователя.
     * @return [Single] с ID добавленной операции.
     */
    @GET("/AddOperation")
    @Headers("Content-Type: application/json")
    fun addOperation(
        @Query("value") value: Int,
        @Query("category") category: String,
        @Query("date") date: String,
        @Query("id") id: Int
    ): Single<Int>

    /**
     * Удаляет операцию по её ID.
     *
     * @param id ID операции.
     * @return [Single] с `true`, если операция была успешно удалена.
     */
    @GET("/DeleteOperation")
    @Headers("Content-Type: application/json")
    fun deleteOperation(@Query("id") id: Int): Single<Boolean>
}

package com.example.uwasting.data.remote

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

/**
 * Интерфейс для взаимодействия с API сайта [statbureau.org](https://www.statbureau.org).
 * Предоставляет доступ к данным по инфляции (ИПЦ) через HTTP-запросы.
 */
interface StatBureauApi {

    /**
     * Получает данные об индексе потребительских цен (ИПЦ) по указанной стране.
     *
     * @param country Название страны (на английском), например: `"Russia"`, `"USA"`, `"Germany"`.
     * @return [Single] — реактивный поток с результатом в виде списка объектов [StatBureauData].
     *
     * Пример запроса:
     * `https://www.statbureau.org/get-data-json?country=Russia`
     */
    @GET("/get-data-json")
    @Headers("Content-Type: application/json")
    fun getIndex(@Query("country") country: String): Single<List<StatBureauData>>
}

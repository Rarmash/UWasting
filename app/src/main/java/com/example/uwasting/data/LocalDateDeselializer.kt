package com.example.uwasting.data

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import java.lang.reflect.Type
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * Десериализатор даты [LocalDate] для библиотеки Gson.
 *
 * Используется при обработке JSON-ответов с сервера, когда дата приходит в виде строки формата `yyyy-MM-dd`.
 * Пример: `"2025-04-23T00:00:00"` → `LocalDate(2025, 4, 23)`
 */
internal class LocalDateDeserializer : JsonDeserializer<LocalDate?> {

    /**
     * Преобразует строку JSON в объект [LocalDate].
     *
     * @param json JSON-элемент, содержащий строку даты.
     * @param typeOfT Тип данных, к которому требуется десериализовать (ожидается [LocalDate]).
     * @param context Контекст десериализации (не используется).
     * @return Объект [LocalDate], извлечённый из строки.
     * @throws JsonParseException в случае ошибки преобразования.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): LocalDate {
        return LocalDate.parse(
            json.asString.substring(0, 10),
            DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH)
        )
    }
}

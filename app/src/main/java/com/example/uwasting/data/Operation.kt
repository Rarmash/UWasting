package com.example.uwasting.data

import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.RequiresApi
import com.google.gson.annotations.SerializedName
import java.sql.Date
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * Модель финансовой операции (дохода или расхода), используемая в приложении UWasting.
 *
 * Представляет собой один элемент в истории пользователя — например, покупку, зарплату, перевод и т.д.
 *
 * @property amount Сумма операции в целых числах.
 * @property category Категория операции (например, "Продукты", "Зарплата").
 * @property date Дата совершения операции ([LocalDate]).
 * @property id Уникальный идентификатор операции.
 */
class Operation(

    @SerializedName("Value")
    var amount: Int,

    @SerializedName("Category")
    var category: String,

    @SerializedName("date")
    var date: LocalDate,

    @SerializedName("Id")
    var id: Int
)

package com.example.uwasting.data.remote

import com.google.gson.annotations.SerializedName

/**
 * Модель данных, получаемая с сайта [statbureau.org](https://www.statbureau.org), содержащая информацию об инфляции.
 *
 * Используется для парсинга JSON-ответа по текущим ИПЦ (индексам потребительских цен).
 *
 * @property inflationRate Текущий уровень инфляции в процентах.
 * @property inflationRateRounded Уровень инфляции, округлённый до ближайшего целого значения.
 * @property inflationRateFormatted Отформатированное значение инфляции (например, "3.1%").
 * @property month Название месяца (на английском, например, "March").
 * @property monthFormatted Форматированное название месяца (например, "Mar 2025").
 * @property country Код страны (целое число, соответствующее внутреннему идентификатору).
 */
class StatBureauData(
    @SerializedName("InflationRate")
    var inflationRate: Float,

    @SerializedName("InflationRateRounded")
    var inflationRateRounded: Float,

    @SerializedName("InflationRateFormatted")
    var inflationRateFormatted: String,

    @SerializedName("Month")
    var month: String,

    @SerializedName("MonthFormatted")
    var monthFormatted: String,

    @SerializedName("Country")
    var country: Int
)

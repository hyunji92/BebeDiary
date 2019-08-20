package com.bebediary.util.extension

import com.bebediary.database.entity.Baby
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.abs

fun Baby.eventDateToText(): String {
    return if (isPregnant) this.dueDateToText() else this.birthdayToText()
}

fun Baby.dueDateToText(): String {

    // 아이 출산 예정일 가져옴
    val date = this.babyDueDate ?: Date()

    // 출산 예정일 280일 이전을 1일로 봄
    // 즉 출산 예정일 - 280 = 임신시작일
    // 현재 날짜 - 임신 시작일 = 임신 주차 계산

    // 임신 시작 날짜
    val pregnantCalendar = Calendar.getInstance().apply {
        time = date
        add(Calendar.DAY_OF_MONTH, -280)
    }

    // 오늘 날짜
    val todayCalendar = Calendar.getInstance()

    // 날짜 차이
    val diffDays = TimeUnit.MILLISECONDS.toDays(pregnantCalendar.timeInMillis - todayCalendar.timeInMillis)

    // 주차, 날짜
    val month = diffDays / 7
    val days = diffDays % 7

    return "임신 '$month'주차 '$days'일"

}

fun Baby.birthdayToText(): String {

    // 아이 생일
    val date = this.birthday ?: Date()

    // 오늘 날짜
    val todayCalendar = Calendar.getInstance()

    // 태어난 후 오늘까지 몇일이 지났는지
    val diffDay = TimeUnit.MILLISECONDS.toDays(abs(date.time - todayCalendar.timeInMillis))

    // 나이 만나이 계산 (태어난 이후 날짜 / 366)
    val age = (diffDay / 365)

    // 개월 수
    val monthToDays = diffDay % 365
    val month = diffDay % 365 / 30

    return "만 ${age}세 ${month}개월 ($monthToDays)일\n태어난지 ${diffDay}일"
}
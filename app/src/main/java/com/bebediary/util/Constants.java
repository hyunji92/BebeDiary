package com.bebediary.util;

/**
 * Created by Pavneet_Singh on 12/31/17.
 */

final public class Constants {
    private Constants() {
    }

    // Request Codes
    public static int requestCameraCode = 0x0101;
    public static int requestAlbumCode = 0x0102;

    // Notification Channel
    public static String notificationChannelIdCalendar = "calendar_alarm";
    public static String notificationChannelNameCalendar = "캘린더 알람";
    public static String notificationChannelDescriptionCalendar = "일정 하루전, 당일에 울리는 알림입니다";

    // Notification
    public static int notificationCalendarRequestCode = 0x01;
}

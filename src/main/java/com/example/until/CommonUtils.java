package com.example.until;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.apache.commons.lang3.RandomStringUtils;

public class CommonUtils {
    public static String getClientId(String key) {
        int length = 10;
        boolean useLetters = true;
        boolean useNumbers = true;
        String generatedString = RandomStringUtils.random(length, useLetters, useNumbers);
        String result = String.format("client-%s-%s", key, generatedString);
        return result;
    }

    public static String convertToAsiaHoChiMinh(LocalDateTime dateTime) {
        ZonedDateTime zonedDateTime = dateTime.atZone(ZoneId.of("UTC"))
                .withZoneSameInstant(ZoneId.of("Asia/Ho_Chi_Minh"));
        return zonedDateTime.toString();
    }


}

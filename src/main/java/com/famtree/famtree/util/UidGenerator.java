package com.famtree.famtree.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class UidGenerator {
    public static String generateFamilyId() {
        return "FAM" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
    }

    public static String generateUserId() {
        return "USR" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
    }

    public static String generateMemberId() {
        return "MEM" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
    }

    public static String generateVerificationCode() {
        int randomNumber = (int) (Math.random() * 90000) + 10000; // 5-digit number
        return "FAM" + randomNumber;
    }
} 
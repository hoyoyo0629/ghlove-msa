package com.ghlove.admin.web.support;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AS-IS {@code saleson.common.security.masking.DefaultDataMasking} 중 주문 목록이 쓰는 4종(NAME·PHONE_NUMBER·ADDRESS_DETAIL·BIRTHDAY)을 그대로
 * 옮긴 것. 알고리즘(별표 개수·정규식·공백 처리)은 원본과 같다 - 결과가 한 글자라도 다르면 AS-IS 화면과 어긋난다.
 */
public final class DataMasking {

    private DataMasking() {
    }

    /** 2글자는 첫 글자만, 3글자 이상은 첫·끝 글자만 남기고 가운데를 *로. 1글자는 그대로. */
    public static String name(String str) {
        if (str == null) {
            return null;
        }
        String pattern = str.length() == 2 ? "^(.)(.+)$" : "^(.)(.+)(.)$";
        Matcher matcher = Pattern.compile(pattern).matcher(str);
        if (!matcher.matches()) {
            return str;
        }
        StringBuilder result = new StringBuilder();
        for (int i = 1; i <= matcher.groupCount(); i++) {
            String target = matcher.group(i);
            result.append(i == 2 ? stars(target.length()) : target);
        }
        return result.toString();
    }

    /** 가운데 번호 묶음만 *로(하이픈이 있으면 그대로 둔다). 형식이 안 맞으면 그대로. */
    public static String phoneNumber(String str) {
        if (str == null) {
            return null;
        }
        Matcher matcher = Pattern.compile("^(\\d{2,3})-?(\\d{3,4})-?(\\d{4})$").matcher(str);
        if (!matcher.matches()) {
            return str;
        }
        boolean hyphen = str.contains("-");
        StringBuilder result = new StringBuilder();
        for (int i = 1; i <= matcher.groupCount(); i++) {
            String target = matcher.group(i);
            result.append(i == 2 ? stars(target.length()) : target);
            if (hyphen && i < matcher.groupCount()) {
                result.append("-");
            }
        }
        return result.toString();
    }

    /** 공백으로 나눈 낱말마다 (글자수-1)개의 *로 바꾸고 뒤에 공백을 붙인다(마지막 낱말 뒤에도 붙는다 - AS-IS 그대로). */
    public static String addressDetail(String str) {
        if (str == null) {
            return null;
        }
        StringBuilder result = new StringBuilder();
        for (String word : str.split(" ")) {
            result.append(stars(Math.max(word.length() - 1, 0))).append(" ");
        }
        return result.toString();
    }

    /** 8자리/10자리면 앞 두 글자 + ** + 다섯 번째 글자부터(10자리 "1980-01-01" → "19**-01-01"). */
    public static String birthday(String str) {
        if (str == null) {
            return null;
        }
        if (str.length() == 8 || str.length() == 10) {
            return str.substring(0, 2) + "**" + str.substring(4);
        }
        return str;
    }

    /** 앞 두 글자와 @ 뒤는 두고 그 사이를 *로(AS-IS Masking.EMAIL, 정규식 {@code ^(..)(.*)(@)(.*)$}). @가 없거나 앞이 두 글자 미만이면 그대로. */
    public static String email(String str) {
        if (str == null) {
            return null;
        }
        Matcher matcher = Pattern.compile("^(..)(.*)([@]{1})(.*)$").matcher(str);
        if (!matcher.matches()) {
            return str;
        }
        StringBuilder result = new StringBuilder();
        for (int i = 1; i <= matcher.groupCount(); i++) {
            String target = matcher.group(i);
            result.append(i == 2 ? stars(target.length()) : target);
        }
        return result.toString();
    }

    private static String stars(int count) {
        char[] c = new char[count];
        Arrays.fill(c, '*');
        return String.valueOf(c);
    }
}

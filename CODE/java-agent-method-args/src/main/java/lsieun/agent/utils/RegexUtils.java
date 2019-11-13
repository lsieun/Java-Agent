package lsieun.agent.utils;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class RegexUtils {

    private static final Map<String, Pattern> patternMap = new HashMap<>();

    public static boolean matches(final String str, final String[] regex_array, final boolean defaultValue) {
        Objects.requireNonNull(str);

        if (regex_array == null || regex_array.length < 1) {
            return defaultValue;
        }

        for(String regex: regex_array) {
            boolean matches = matches(str, regex);
            if (matches) return true;
        }
        return false;
    }

    public static boolean matches(String str, String regex) {
        Objects.requireNonNull(str);
        Objects.requireNonNull(regex);

        Pattern p = patternMap.get(regex);
        if (p == null) {
            p = Pattern.compile(regex);
            patternMap.put(regex, p);
        }

        if (p.matcher(str).matches()) return true;
        return false;
    }

}

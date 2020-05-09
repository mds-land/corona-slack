package com.github.mdsina.corona.util;

import io.micronaut.core.util.StringUtils;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public class BodyUtils {

    public static Map<String, String> parseFormToMap(String body) {
        String[] pairs = StringUtils.tokenizeToStringArray(body, "&");

        LinkedHashMap<String, String> result = new LinkedHashMap<>(pairs.length);

        Charset charset = StandardCharsets.UTF_8;
        for (String pair : pairs) {
            int idx = pair.indexOf('=');
            if (idx == -1) {
                result.put(URLDecoder.decode(pair, charset), null);
            }
            else {
                String name = URLDecoder.decode(pair.substring(0, idx), charset);
                String value = URLDecoder.decode(pair.substring(idx + 1), charset);
                result.put(name, value);
            }
        }

        return result;
    }
}

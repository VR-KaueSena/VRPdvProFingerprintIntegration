package br.com.vrsoftware.vrpdvprofingerprintintegration.utils;

import java.util.HashMap;
import java.util.Map;

/// Utility class responsible for parsing HTTP query strings.
///
/// Converts a raw query string (e.g. "key1=value1&key2=value2")
/// into a Map of parameter names and values.
///
/// This parser is intentionally simple and assumes parameters
/// are not URL-encoded.
public class StringParser {

    /// Parses the given query string into a map of key-value pairs.
    ///
    /// @param query the raw query string from the HTTP request
    /// @return a map containing parsed query parameters
    public static Map<String, String> parse(String query) {
        Map<String, String> map = new HashMap<>();
        if (query == null) return map;

        for (String param : query.split("&")) {
            String[] pair = param.split("=");
            if (pair.length == 2) map.put(pair[0], pair[1]);
        }

        return map;
    }
}

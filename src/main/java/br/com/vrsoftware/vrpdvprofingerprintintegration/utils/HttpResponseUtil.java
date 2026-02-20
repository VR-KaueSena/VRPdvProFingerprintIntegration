package br.com.vrsoftware.vrpdvprofingerprintintegration.utils;

import br.com.vrsoftware.vrpdvprofingerprintintegration.fingerprint.exceptions.FingerprintException;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Map;
/**
 * Utility methods for standardized HTTP JSON responses.
 */
public final class HttpResponseUtil {
    /**
     * Sends a successful JSON response.
     */
    public static void sendSuccess(
            HttpExchange exchange,
            int statusCode,
            String message,
            Map<String, Object> data
    ) throws IOException {

        String json = "{"
                + "\"error\":false,"
                + "\"message\":" + toJsonString(message) + ","
                + "\"data\":" + toJsonObject(data)
                + "}";

        sendJson(exchange, statusCode, json);
    }

    /**
     * Sends an internal error response (HTTP 500).
     */
    public static void sendError(
            HttpExchange exchange,
            String message
    ) throws IOException {

        String msg = (message == null || message.trim().isEmpty())
                ? "Internal server error"
                : message;

        sendJson(exchange, 500,
                "{\"error\":true,\"message\":\"" + escapeJson(msg) + "\"}"
        );
    }

    private static String escapeJson(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }

    /**
     * Sends a fingerprint domain error (HTTP 400).
     */
    public static void sendFingerprintError(
            HttpExchange exchange,
            FingerprintException e
    ) throws IOException {

        String json = "{"
                + "\"error\":true,"
                + "\"code\":\"" + e.getCode() + "\","
                + "\"message\":\"" + e.getMessage() + "\""
                + "}";

        sendJson(exchange, 400, json);
    }

    /**
     * Extracts the endpoint name from the request path.
     */
    public static String extractEndpoint(String path) {
        int idx = path.lastIndexOf('/');
        return idx >= 0 ? path.substring(idx + 1) : "";
    }

    /**
     * Writes a JSON response with proper headers.
     */
    private static void sendJson(
            HttpExchange exchange,
            int statusCode,
            String json
    ) throws IOException {

        byte[] resp = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, resp.length);
        exchange.getResponseBody().write(resp);
    }

    /**
     * Converts a string to a JSON-safe value.
     */
    private static String toJsonString(String value) {
        if (value == null) return "null";
        return "\"" + value.replace("\"", "\\\"") + "\"";
    }

    /**
     * Converts a map into a JSON object.
     */
    private static String toJsonObject(Map<String, Object> map) {
        if (map == null || map.isEmpty()) return "{}";

        StringBuilder json = new StringBuilder("{");
        Iterator<Map.Entry<String, Object>> it = map.entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry<String, Object> entry = it.next();
            json.append("\"")
                    .append(entry.getKey())
                    .append("\":")
                    .append(toJsonValue(entry.getValue()));

            if (it.hasNext()) json.append(",");
        }

        json.append("}");
        return json.toString();
    }

    /**
     * Converts supported Java types into JSON values.
     */
    private static String toJsonValue(Object value) {
        if (value == null) return "null";
        if (value instanceof Number || value instanceof Boolean) {
            return value.toString();
        }
        return toJsonString(value.toString());
    }
}

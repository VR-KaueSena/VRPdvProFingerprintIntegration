package br.com.vrsoftware.vrpdvprofingerprintintegration.utils;

import br.com.vrsoftware.vrpdvprofingerprintintegration.fingerprint.exceptions.FingerprintException;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Map;

/// Utility class for standardized HTTP JSON responses.
public final class HttpResponseUtil {
    /// Sends a standardized success response.
    ///
    /// Response format:
    /// {
    ///   "error": false,
    ///   "message": "...",
    ///   "data": { ... }
    /// }
    ///
    /// - message: optional (can be null)
    /// - data: flexible payload using Map<String, Object>
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

    /// Handles unexpected internal errors.
    ///
    /// Used for unhandled exceptions, bugs or infrastructure failures.
    ///
    /// Response format:
    /// {
    ///   "error": true,
    ///   "message": "Internal server error"
    /// }
    ///
    /// HTTP Status: 500
    public static void sendError(
            HttpExchange exchange
    ) throws IOException {

        String json = "{"
                + "\"error\":true,"
                + "\"message\":\"Internal server error\""
                + "}";

        sendJson(exchange, 500, json);
    }

    /// Handles known fingerprint-related errors.
    ///
    /// Used for expected domain/device errors such as:
    /// - device not initialized
    /// - invalid capture state
    /// - capture timeout
    ///
    /// Response format:
    /// {
    ///   "error": true,
    ///   "code": "ERROR_CODE",
    ///   "message": "Description"
    /// }
    ///
    /// HTTP Status: 400
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

    /// Extracts the endpoint name from a request path.
    ///
    /// Example:
    /// /fingerprint/init -> init
    public static String extractEndpoint(String path) {
        int idx = path.lastIndexOf('/');
        return idx >= 0 ? path.substring(idx + 1) : "";
    }

    /// Writes a JSON response with correct headers.
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

    /// Safely converts a string into a JSON string value.
    private static String toJsonString(String value) {
        if (value == null) return "null";
        return "\"" + value.replace("\"", "\\\"") + "\"";
    }

    /// Converts a map into a JSON object.
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

    /// Converts supported Java types into JSON values.
    private static String toJsonValue(Object value) {
        if (value == null) return "null";
        if (value instanceof Number || value instanceof Boolean) {
            return value.toString();
        }
        return toJsonString(value.toString());
    }
}

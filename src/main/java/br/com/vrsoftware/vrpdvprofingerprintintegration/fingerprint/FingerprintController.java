package br.com.vrsoftware.vrpdvprofingerprintintegration.fingerprint;

import br.com.vrsoftware.vrpdvprofingerprintintegration.fingerprint.exceptions.FingerprintException;
import br.com.vrsoftware.vrpdvprofingerprintintegration.fingerprint.factory.FingerprintFactory;
import br.com.vrsoftware.vrpdvprofingerprintintegration.fingerprint.model.FingerprintMatchResult;
import br.com.vrsoftware.vrpdvprofingerprintintegration.fingerprint.model.FingerprintType;
import br.com.vrsoftware.vrpdvprofingerprintintegration.utils.StringParser;
import br.com.vrsoftware.vrpdvprofingerprintintegration.utils.HttpResponseUtil;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * HTTP controller for fingerprint operations.
 *
 * <p>Exposes endpoints to initialize, capture, compare and
 * shutdown fingerprint devices.</p>
 *
 * <p>Business logic is delegated to the fingerprint core layer.</p>
 */
public class FingerprintController implements HttpHandler {

    private static Fingerprint fingerprint = null;

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        String path = exchange.getRequestURI().getPath();
        String endpoint = HttpResponseUtil.extractEndpoint(path);

        try {
            switch (endpoint) {

                // GET /fingerprint/init
                case "init":
                    initialize(exchange);
                    break;

                // GET /fingerprint/capture
                case "capture":
                    capture(exchange);
                    break;

                // POST /fingerprint/match
                case "match":
                    match(exchange);
                    break;

                // GET /fingerprint/shutdown
                case "shutdown":
                    shutdown(exchange);
                    break;

                default:
                    exchange.sendResponseHeaders(404, -1);
            }

        } catch (FingerprintException e) {
            HttpResponseUtil.sendFingerprintError(exchange, e);
        } catch (Exception e) {
            HttpResponseUtil.sendError(exchange, e.getMessage());
        } finally {
            exchange.close();
        }
    }

    /**
     * Initializes the fingerprint device.
     *
     * <p>Parses request parameters, creates the correct implementation
     * and initializes the device.</p>
     */
    private void initialize(HttpExchange exchange) throws IOException {
        Map<String, String> params = StringParser.parse(
                exchange.getRequestURI().getQuery()
        );

        int modelId = Integer.parseInt(params.get("modelId"));
        FingerprintType type = FingerprintType.fromId(modelId);

        if (fingerprint == null) fingerprint = FingerprintFactory.create(type);

        try {
            fingerprint.initialize();
        } catch (Exception e) {
            HttpResponseUtil.sendError(
                    exchange,
                    "Fingerprint device not connected"
            );

            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("device", type.name());

        HttpResponseUtil.sendSuccess(
                exchange,
                200,
                "CONNECTED",
                data
        );
    }


    /**
     * Captures a fingerprint template.
     *
     * <p>Validates device state, starts capture and returns
     * the captured template.</p>
     */
    private void capture(HttpExchange exchange) throws Exception{
        fingerprint.startCapture();

        while(!fingerprint.hasFingerprintCapture()) {
            Thread.sleep(50);
        }

        String fingerprintCaptured = fingerprint.fingerprintCaptured();
        fingerprint.stopCapture();

        Map<String, Object> data = new HashMap<>();
        data.put("fingerprint", fingerprintCaptured);

        HttpResponseUtil.sendSuccess(
                exchange,
                200,
                null,
                data
        );
    }

    /**
     * Compares two fingerprint templates.
     *
     * <p>Extracts templates from the request and returns
     * the match result.</p>
     */
    private void match(HttpExchange exchange) throws Exception {
        String body = new String(readAllBytes(exchange.getRequestBody()), StandardCharsets.UTF_8);

        JsonObject json = JsonParser.parseString(body).getAsJsonObject();

        String digitalCaptured = json.get("digitalCaptured").getAsString();
        String digitalToCompare = json.get("digitalToCompare").getAsString();

        FingerprintMatchResult matchResult = fingerprint.match(
                digitalCaptured,
                digitalToCompare
        );

        Map<String, Object> data = new HashMap<>();
        data.put("matched", matchResult.matched());
        data.put("score", matchResult.score());

        HttpResponseUtil.sendSuccess(
                exchange,
                200,
                null,
                data
        );
    }

    /**
     * Shuts down the fingerprint device and releases resources.
     */
    private void shutdown(HttpExchange exchange) throws Exception {
        if (fingerprint != null) {
            fingerprint.shutdown();
            fingerprint = null;
        }

        HttpResponseUtil.sendSuccess(
                exchange,
                200,
                "SHUTDOWN",
                null
        );
    }

    /**
     * Reads all bytes from an input stream.
     */
    private static byte[] readAllBytes(InputStream input) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[4096];
        int nRead;

        while ((nRead = input.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }

        return buffer.toByteArray();
    }
}

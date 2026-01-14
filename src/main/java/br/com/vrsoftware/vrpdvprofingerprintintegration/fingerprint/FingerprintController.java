package br.com.vrsoftware.vrpdvprofingerprintintegration.fingerprint;

import br.com.vrsoftware.vrpdvprofingerprintintegration.fingerprint.exceptions.FingerprintException;
import br.com.vrsoftware.vrpdvprofingerprintintegration.fingerprint.factory.FingerprintFactory;
import br.com.vrsoftware.vrpdvprofingerprintintegration.fingerprint.model.FingerprintType;
import br.com.vrsoftware.vrpdvprofingerprintintegration.utils.StringParser;
import br.com.vrsoftware.vrpdvprofingerprintintegration.utils.HttpResponseUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/// HTTP controller responsible for fingerprint operations.
///
/// This controller exposes a REST-like API to:
/// - initialize the fingerprint device
/// - capture a fingerprint
/// - compare fingerprint templates
/// - shutdown the fingerprint device
///
/// This class is stateless and does not hold business state.
/// All fingerprint logic is delegated to the core layer.
public class FingerprintController implements HttpHandler {

    private static final Logger logger =
            Logger.getLogger(FingerprintController.class.getName());

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

                // POST /fingerprint/shutdown
                case "shutdown":
                    shutdown(exchange);
                    break;

                default:
                    exchange.sendResponseHeaders(404, -1);
            }

        } catch (FingerprintException e) {
            HttpResponseUtil.sendFingerprintError(exchange, e);
        } catch (Exception e) {
            HttpResponseUtil.sendError(exchange);
        } finally {
            exchange.close();
        }
    }

    /// Initializes the fingerprint device.
    ///
    /// Responsibilities:
    /// - Parse query or body parameters (model, sensitivity)
    /// - Create the correct fingerprint implementation
    /// - Initialize the SDK/device
    private void initialize(HttpExchange exchange) throws IOException  {
        Map<String, String> params = StringParser.parse(
                exchange.getRequestURI().getQuery()
        );

        int modelId = Integer.parseInt(params.get("modelo"));
        FingerprintType type = FingerprintType.fromId(modelId);

        // Create fingerprint implementation
        fingerprint = FingerprintFactory.create(type);

        // Initialize asynchronously to avoid blocking HTTP thread
        new Thread(() -> {
            try {
                fingerprint.initialize();
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Failed to initialize fingerprint device", e);
            }
        }, "fingerprint-init-thread").start();

        Map<String, Object> data = new HashMap<>();
        data.put("device", type.name());

        HttpResponseUtil.sendSuccess(
                exchange,
                200,
                "CONNECTED",
                data
        );
    }

    /// Captures a fingerprint template.
    ///
    /// Responsibilities:
    /// - Validate device state
    /// - Start capture process
    /// - Block until capture completes
    /// - Return captured template
    private void capture(HttpExchange exchange) throws Exception {
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

    /// Compares two fingerprint templates.
    ///
    /// Responsibilities:
    /// - Parse request body
    /// - Extract captured and stored templates
    /// - Call fingerprint.match(...)
    /// - Return match result and score
    private void match(HttpExchange exchange) throws Exception {
        // TODO parse JSON body
        // TODO call match
        // TODO write result response
    }

    /// Shuts down the fingerprint device and releases resources.
    private void shutdown(HttpExchange exchange) throws Exception {
        // TODO fingerprint.shutdown()
        // TODO write success response
    }
}

package br.com.vrsoftware.vrpdvprofingerprintintegration;

import br.com.vrsoftware.vrpdvprofingerprintintegration.fingerprint.FingerprintController;
import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

/**
 * Application entry point for the Fingerprint Integration Service.
 *
 * <p>This class is responsible for:</p>
 * <ul>
 *   <li>Starting the embedded HTTP server</li>
 *   <li>Registering HTTP endpoints</li>
 *   <li>Wiring controllers</li>
 * </ul>
 *
 * <p>Business logic and biometric handling are delegated to the
 * fingerprint core and controller layers.</p>
 */
public class Main {

    /**
     * Default HTTP port used by the fingerprint service.
     */
    private static final int PORT = 8090;

    /**
     * Application bootstrap.
     *
     * @param args command-line arguments
     * @throws Exception if server initialization fails
     */
    public static void main(String[] args) throws Exception {

        // Create embedded HTTP server
        HttpServer server = HttpServer.create(
                new InetSocketAddress(PORT),
                0
        );

        // Configure a fixed thread pool for handling HTTP requests
        server.setExecutor(Executors.newFixedThreadPool(10));

        // Health check endpoint used by external services (e.g. Go API)
        server.createContext("/health", exchange -> {
            byte[] resp = "OK".getBytes();
            exchange.sendResponseHeaders(200, resp.length);
            exchange.getResponseBody().write(resp);
            exchange.close();
        });

        // Fingerprint API endpoints
        server.createContext("/fingerprint", new FingerprintController());

        // Start HTTP server
        server.start();

        System.out.println(
                "Fingerprint service running on port " + PORT
        );
    }
}
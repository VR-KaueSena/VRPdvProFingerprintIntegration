package br.com.vrsoftware.vrpdvprofingerprintintegration.fingerprint;

import br.com.vrsoftware.vrpdvprofingerprintintegration.fingerprint.model.FingerprintMatchResult;

/**
 * Abstraction for fingerprint biometric devices.
 *
 * <p>Defines a unified API for device lifecycle, capture and matching,
 * independent of vendor SDKs.</p>
 */
public interface Fingerprint {

    /**
     * Default threshold used for fingerprint matching.
     */
    int DEFAULT_THRESHOLD = 50;

    /**
     * Initializes the biometric device.
     */
    void initialize() throws Exception;

    /**
     * Starts the fingerprint capture process.
     */

    void startCapture() throws Exception;

    /**
     * Stops an ongoing fingerprint capture.
     */
    void stopCapture() throws Exception;

    /**
     * Releases device and SDK resources.
     */
    void shutdown() throws Exception;

    /**
     * Compares two fingerprint templates.
     */
    FingerprintMatchResult match(
            String capturedTemplate,
            String storedTemplate
    ) throws Exception;

    /**
     * Indicates whether fingerprint capture has completed.
     */
    boolean hasFingerprintCapture();

    /**
     * Returns the captured fingerprint template.
     */
    String fingerprintCaptured();
}

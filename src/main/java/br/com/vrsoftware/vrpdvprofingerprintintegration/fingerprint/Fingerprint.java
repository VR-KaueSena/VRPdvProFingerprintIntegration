package br.com.vrsoftware.vrpdvprofingerprintintegration.fingerprint;

import br.com.vrsoftware.vrpdvprofingerprintintegration.fingerprint.model.FingerprintMatchResult;

/// Represents a generic fingerprint biometric device abstraction.
///
/// This interface defines the core operations required to:
/// - initialize a fingerprint reader
/// - capture fingerprint templates
/// - compare fingerprint templates
/// - manage device lifecycle and sensitivity
///
/// Implementations of this interface must encapsulate all vendor-specific
/// SDKs (e.g. ControlID, DigitalPersona) and expose a unified API to the system.
///
/// This interface is intentionally independent of:
/// - HTTP / API layers
/// - PDV logic
/// - persistence or database concerns
public interface Fingerprint {

    /// Default minimum threshold used to compare fingerprint similarity.
    /// Higher values mean stricter matching.
    int DEFAULT_THRESHOLD = 50;

    /// Initializes the biometric device and underlying SDK.
    void initialize() throws Exception;

    /// Starts a fingerprint capture process using default behavior.
    void startCapture() throws Exception;

    /// Stops any ongoing fingerprint capture process.
    void stopCapture() throws Exception;

    /// Properly releases the biometric device and SDK resources.
    void shutdown() throws Exception;

    /// Compares two fingerprint templates and returns whether they match.
    FingerprintMatchResult match(
            String capturedTemplate,
            String storedTemplate
    ) throws Exception;

    /// Indicates whether the fingerprint capture process has completed.
    boolean hasFingerprintCapture();

    /// Returns the captured fingerprint template in its serialized form.
    /// Should only be called after captureCompleted() returns true.
    String fingerprintCaptured();
}

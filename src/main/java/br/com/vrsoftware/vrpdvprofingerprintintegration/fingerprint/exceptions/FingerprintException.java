package br.com.vrsoftware.vrpdvprofingerprintintegration.fingerprint.exceptions;

/// Represents a controlled exception thrown by the fingerprint core.
///
/// This exception is designed to be safely exposed through HTTP APIs,
/// allowing other services (e.g. Go API) to consume error information
/// in a structured and predictable way.
public class FingerprintException extends RuntimeException {

    /// Machine-readable error code (used by external APIs).
    private final String code;

    /// Creates a fingerprint exception with an error code and message.
    public FingerprintException(String code, String message) {
        super(message);
        this.code = code;
    }

    /// Returns the machine-readable error code.
    public String getCode() {
        return code;
    }
}


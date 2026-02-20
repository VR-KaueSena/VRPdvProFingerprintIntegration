package br.com.vrsoftware.vrpdvprofingerprintintegration.fingerprint.exceptions;

/**
 * Exception representing controlled fingerprint errors.
 *
 * <p>Designed to be safely exposed through HTTP APIs.</p>
 */
public class FingerprintException extends RuntimeException {

    /**
     * Machine-readable error code.
     */
    private final String code;

    /**
     * Creates a fingerprint exception with code and message.
     */
    public FingerprintException(String code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * Returns the machine-readable error code.
     */
    public String getCode() {
        return code;
    }
}


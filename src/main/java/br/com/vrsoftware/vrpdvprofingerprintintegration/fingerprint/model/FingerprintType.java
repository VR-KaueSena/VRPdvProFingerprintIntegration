package br.com.vrsoftware.vrpdvprofingerprintintegration.fingerprint.model;

/**
 * Supported fingerprint device types.
 */
public enum FingerprintType {
    UNKNOWN(0),
    CONTROLID(1),
    HAMSTER(2);

    private final int id;

    FingerprintType(int id) {
        this.id = id;
    }

    /**
     * Resolves a fingerprint type from its numeric identifier.
     */
    public static FingerprintType fromId(int id) {
        for (FingerprintType type : values()) {
            if (type.id == id) return type;
        }

        throw new IllegalArgumentException("Invalid fingerprint type: " + id);
    }
}

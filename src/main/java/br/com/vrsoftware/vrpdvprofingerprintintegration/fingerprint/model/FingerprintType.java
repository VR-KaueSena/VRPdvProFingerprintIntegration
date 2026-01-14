package br.com.vrsoftware.vrpdvprofingerprintintegration.fingerprint.model;

public enum FingerprintType {
    UNKNOWN(0),
    GENERIC(1),
    CONTROLID(2),
    HAMSTER(3),
    SMAKBIO(6);


    private final int id;

    FingerprintType(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static FingerprintType fromId(int id) {
        for (FingerprintType type : values()) {
            if (type.id == id) return type;

        }
        throw new IllegalArgumentException("Invalid fingerprint type: " + id);
    }
}

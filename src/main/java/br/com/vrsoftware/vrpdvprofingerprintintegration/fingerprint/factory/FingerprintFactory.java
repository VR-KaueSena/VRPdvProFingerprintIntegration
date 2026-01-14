package br.com.vrsoftware.vrpdvprofingerprintintegration.fingerprint.factory;

import br.com.vrsoftware.vrpdvprofingerprintintegration.fingerprint.Fingerprint;
import br.com.vrsoftware.vrpdvprofingerprintintegration.fingerprint.exceptions.FingerprintErrorCodes;
import br.com.vrsoftware.vrpdvprofingerprintintegration.fingerprint.exceptions.FingerprintException;
import br.com.vrsoftware.vrpdvprofingerprintintegration.fingerprint.impl.ControlID;
import br.com.vrsoftware.vrpdvprofingerprintintegration.fingerprint.model.FingerprintType;

/// Factory responsible for creating fingerprint implementations based on device type.
public final class FingerprintFactory {

    public static Fingerprint create(FingerprintType type) {

        return switch (type) {
            case CONTROLID, SMAKBIO -> new ControlID();
            default -> throw new FingerprintException(
                    FingerprintErrorCodes.UNSUPPORTED_DEVICE,
                    "Fingerprint device not supported: " + type
            );
        };
    }
}

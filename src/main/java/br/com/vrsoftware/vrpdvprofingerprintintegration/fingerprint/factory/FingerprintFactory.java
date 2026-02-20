package br.com.vrsoftware.vrpdvprofingerprintintegration.fingerprint.factory;

import br.com.vrsoftware.vrpdvprofingerprintintegration.fingerprint.Fingerprint;
import br.com.vrsoftware.vrpdvprofingerprintintegration.fingerprint.exceptions.FingerprintErrorCodes;
import br.com.vrsoftware.vrpdvprofingerprintintegration.fingerprint.exceptions.FingerprintException;
import br.com.vrsoftware.vrpdvprofingerprintintegration.fingerprint.impl.ControlID;
import br.com.vrsoftware.vrpdvprofingerprintintegration.fingerprint.impl.HamsterDX;
import br.com.vrsoftware.vrpdvprofingerprintintegration.fingerprint.model.FingerprintType;

/**
 * Factory for creating fingerprint implementations.
 */
public final class FingerprintFactory {

    /**
     * Creates a fingerprint implementation based on the device type.
     */
    public static Fingerprint create(FingerprintType type) {

        switch (type) {
            case CONTROLID:
                return new ControlID();
            case HAMSTER:
                return new HamsterDX();
            default:
                throw new FingerprintException(
                        FingerprintErrorCodes.UNSUPPORTED_DEVICE,
                        "Fingerprint device not supported: " + type
                );
        }
    }
}

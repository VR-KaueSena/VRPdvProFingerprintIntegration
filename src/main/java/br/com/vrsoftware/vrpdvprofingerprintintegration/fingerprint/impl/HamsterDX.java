package br.com.vrsoftware.vrpdvprofingerprintintegration.fingerprint.impl;

import br.com.vrsoftware.vrpdvprofingerprintintegration.fingerprint.exceptions.FingerprintErrorCodes;
import br.com.vrsoftware.vrpdvprofingerprintintegration.fingerprint.exceptions.FingerprintException;
import com.machinezoo.sourceafis.FingerprintMatcher;
import com.machinezoo.sourceafis.FingerprintTemplate;
import com.nitgen.SDK.BSP.NBioBSPJNI;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import br.com.vrsoftware.vrpdvprofingerprintintegration.fingerprint.Fingerprint;
import br.com.vrsoftware.vrpdvprofingerprintintegration.fingerprint.model.FingerprintMatchResult;

import javax.imageio.ImageIO;

/**
 * Fingerprint implementation for Hamster DX devices.
 */
public class HamsterDX  implements Fingerprint {
    private boolean sdkInitialized = false;
    private boolean readerConnected = false;
    private boolean captureCompleted = false;
    private boolean awaitingCapture = false;

    private ThreadReading threadReading = new ThreadReading();
    private final List<FingerprintTemplate> fingerprintsCaptured = new ArrayList<>();

    NBioBSPJNI bsp = null;
    NBioBSPJNI.INPUT_FIR inputFIR = null;
    NBioBSPJNI.WINDOW_OPTION winOption;

    private NBioBSPJNI.DEVICE_ENUM_INFO deviceEnumInfo;
    private NBioBSPJNI.FIR_HANDLE handleReader;

    private short deviceId = 0;

    public HamsterDX() {
        bsp = new NBioBSPJNI();
        inputFIR = bsp.new INPUT_FIR();
        winOption = bsp.new WINDOW_OPTION();

        winOption.WindowStyle = NBioBSPJNI.WINDOW_STYLE.INVISIBLE;
        winOption.CaptureCallback = new NBioBSPJNI.CAPTURE_CALLBACK() {
            @Override
            public int OnCaptured(NBioBSPJNI.CAPTURED_DATA i_capture) {
                if (i_capture.ImageQuality > 30) {

                    try {
                        byte[] bitmapBytes;
                        try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
                            ImageIO.write((java.awt.image.BufferedImage) i_capture.RawImage, "bmp", stream);
                            stream.flush();
                            bitmapBytes = stream.toByteArray();
                        }

                        FingerprintTemplate fp = new FingerprintTemplate(bitmapBytes);
                        fingerprintsCaptured.add(fp);

                        return NBioBSPJNI.ERROR.NBioAPIERROR_NONE;

                    } catch (Exception ex) {
                        return NBioBSPJNI.ERROR.NBioAPIERROR_FUNCTION_FAIL;
                    }
                }

                if (!awaitingCapture) return NBioBSPJNI.ERROR.NBioAPIERROR_USER_CANCEL;

                return NBioBSPJNI.ERROR.NBioAPIERROR_NONE;
            }
        };

    }

    @Override
    public boolean hasFingerprintCapture() {  return !fingerprintsCaptured.isEmpty(); }

    @Override
    public String fingerprintCaptured() {
        if (fingerprintsCaptured.isEmpty()) return "";

        return fingerprintsCaptured.get(fingerprintsCaptured.size() - 1).json();
    }

    @Override
    public void initialize() throws Exception {
        if (sdkInitialized) return;

        if (deviceId > 0) {
            bsp.CloseDevice(deviceEnumInfo.DeviceInfo[0].NameID, deviceEnumInfo.DeviceInfo[0].Instance);
        }

        deviceEnumInfo = bsp.new DEVICE_ENUM_INFO();
        bsp.EnumerateDevice(deviceEnumInfo);
        bsp.GetVersion();

        if (deviceEnumInfo.DeviceCount <= 0) {
            sdkInitialized = false;
            readerConnected = false;

            throw new FingerprintException(
                    FingerprintErrorCodes.SDK_INIT_FAILED,
                    "Unable to initialize fingerprint reader"
            );
        }

        bsp.OpenDevice(deviceEnumInfo.DeviceInfo[0].NameID, deviceEnumInfo.DeviceInfo[0].Instance);

        deviceId = bsp.GetOpenedDeviceID();

        if (bsp.IsErrorOccured()) {
            throw new FingerprintException(
                    FingerprintErrorCodes.SDK_INIT_FAILED,
                    "Unable to initialize fingerprint reader"
            );
        }

        readerConnected = true;
        sdkInitialized = true;
        awaitingCapture = false;
    }

    @Override
    public void startCapture() throws Exception {
        if (awaitingCapture) return;

        if (!sdkInitialized) {
            throw new FingerprintException(
                    FingerprintErrorCodes.READER_NOT_CONNECTED,
                    "Unable to connect to reader"
            );
        }


        fingerprintsCaptured.clear();
        awaitingCapture = true;

        if (threadReading.isAlive()) threadReading.interrupt();

        threadReading = new ThreadReading();
        threadReading.setName("ThreadReading - ControlID");
        threadReading.start();
    }

    @Override
    public void stopCapture() throws Exception {
        if (!awaitingCapture) return;

        if (threadReading.isAlive()) threadReading.interrupt();

        fingerprintsCaptured.clear();
        awaitingCapture = false;
    }

    @Override
    public void shutdown() throws Exception {
        if (!sdkInitialized || !readerConnected) {
            throw new FingerprintException(
                    FingerprintErrorCodes.READER_NOT_CONNECTED,
                    "Unable to connect to reader"
            );
        }

        if (awaitingCapture) {
            if (threadReading.isAlive()) threadReading.interrupt();
            fingerprintsCaptured.clear();
            awaitingCapture = false;
        }

        while(!captureCompleted) {
            Thread.sleep(1000);
            captureCompleted = true;
        }

        if (handleReader != null) handleReader.dispose();

        if (deviceId > 0) {
            bsp.CloseDevice(
                    deviceEnumInfo.DeviceInfo[0].NameID,
                    deviceEnumInfo.DeviceInfo[0].Instance
            );
        }

        deviceId = 0;

        bsp.dispose();

        readerConnected = false;
        sdkInitialized = false;
    }

    @Override
    public FingerprintMatchResult match(String capturedTemplate, String storedTemplate) {
        try {
            FingerprintTemplate fpCaptured = new FingerprintTemplate(capturedTemplate);
            FingerprintTemplate fpStored = new FingerprintTemplate(storedTemplate);
            FingerprintMatcher fm = new FingerprintMatcher(fpCaptured);

            double score = fm.match(fpStored);
            int margin = 10;

            double threshold = Fingerprint.DEFAULT_THRESHOLD - margin;
            boolean matched = score >= threshold;

            return new FingerprintMatchResult(matched, score);
        } catch (Exception ex) {
            throw new FingerprintException(
                    FingerprintErrorCodes.MATCH_FAILED,
                    "Fingerprint match failed"
            );
        }
    }

    /**
     * Thread responsible for capturing a single fingerprint.
     *
     * <p>Waits until the reader is ready, performs capture attempts
     * and stores the resulting template when successful.</p>
     */
    private class ThreadReading extends Thread {

        /**
         * Capture execution loop.
         */
        @Override
        public void run() {
            try {
                captureCompleted = false;

                while (awaitingCapture && !Thread.currentThread().isInterrupted()) {

                    handleReader = bsp.new FIR_HANDLE();

                    if (fingerprintsCaptured.size() >= 3) break;

                    sleep(500);

                    bsp.Capture(
                            NBioBSPJNI.FIR_PURPOSE.VERIFY,
                            handleReader, -1,
                            null,
                            winOption
                            );

                    if (Thread.currentThread().isInterrupted())   break;

                    if (bsp.IsErrorOccured()) {
                        if (bsp.GetErrorCode() == NBioBSPJNI.ERROR.NBioAPIERROR_CAPTURE_TIMEOUT) continue;

                        break;
                    }

                    handleReader.dispose();
                    handleReader = null;
                    break;
                }

            } catch (Exception ex) {
                readerConnected = false;
                throw new FingerprintException(
                        FingerprintErrorCodes.CAPTURE_FAILED,
                        "Capture failed"
                );
            } finally {
                awaitingCapture = false;
                captureCompleted = true;
            }
        }
    }
}

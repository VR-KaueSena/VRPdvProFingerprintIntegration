package br.com.vrsoftware.vrpdvprofingerprintintegration.fingerprint.impl;

import CIDBio.CIDBio;
import CIDBio.ConfigParam;
import CIDBio.RetCode;
import CIDBio.Image;

import br.com.vrsoftware.vrpdvprofingerprintintegration.fingerprint.Fingerprint;
import br.com.vrsoftware.vrpdvprofingerprintintegration.fingerprint.exceptions.FingerprintErrorCodes;
import br.com.vrsoftware.vrpdvprofingerprintintegration.fingerprint.exceptions.FingerprintException;
import br.com.vrsoftware.vrpdvprofingerprintintegration.fingerprint.model.FingerprintMatchResult;
import com.machinezoo.sourceafis.FingerprintMatcher;
import com.machinezoo.sourceafis.FingerprintTemplate;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class ControlID implements Fingerprint {
    private boolean sdkInitialized = false;
    private boolean readerConnected = false;
    private boolean captureCompleted = false;
    private boolean awaitingCapture = false;

    private ThreadReading threadReading = new ThreadReading();
    private final List<FingerprintTemplate> fingerprintsCaptured = new ArrayList<>();


    private CIDBio scanner;

    @Override
    public boolean hasFingerprintCapture() {  return !fingerprintsCaptured.isEmpty(); }

    @Override
    public String fingerprintCaptured () {
        if (fingerprintsCaptured.isEmpty())  return "";

        return fingerprintsCaptured.getLast().json();
    }

    @Override
    public void initialize() {

        if (sdkInitialized) return;

        scanner = new CIDBio();
        RetCode ret =  CIDBio.Init();

        if (ret != RetCode.SUCCESS && ret != RetCode.WARNING_ALREADY_INIT) {
            sdkInitialized = false;
            readerConnected = false;

            throw new FingerprintException(
                    FingerprintErrorCodes.SDK_INIT_FAILED,
                    "Unable to initialize fingerprint reader"
            );
        }

        readerConnected = true;
        sdkInitialized = true;
        awaitingCapture = false;

        scanner.SetParameter(ConfigParam.BUZZER_ON, "0");
        scanner.SetParameter(ConfigParam.DETECT_TIMEOUT, "3000");
    }

    @Override
    public void startCapture()  {
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
    public void stopCapture() {
       if (!awaitingCapture) return;

       if (threadReading.isAlive()) threadReading.interrupt();

       fingerprintsCaptured.clear();
       awaitingCapture = false;
    }

    @Override
    public void shutdown() throws Exception{
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
            scanner.CancelCapture();
            Thread.sleep(1000);
            captureCompleted = true;
        }

        RetCode ret = CIDBio.Terminate();

        readerConnected = false;
        sdkInitialized = false;

        if (ret != RetCode.SUCCESS) CIDBio.Terminate();
    }

    @Override
    public FingerprintMatchResult match(String capturedTemplate, String storedTemplate) {
        try {
            FingerprintTemplate fpCaptured = new FingerprintTemplate(capturedTemplate.getBytes());
            FingerprintTemplate fpStored = new FingerprintTemplate(storedTemplate.getBytes());
            FingerprintMatcher fm = new FingerprintMatcher(fpCaptured);

            double score = fm.match(fpStored);

            // Calculates the matching threshold based on the configured sensitivity (e.g. DEFAULT_THRESHOLD = 50 results in a threshold of ~12.5).
            double threshold = Fingerprint.DEFAULT_THRESHOLD * 0.25;
            boolean matched = score >= threshold;

            return new FingerprintMatchResult(matched, score);
        } catch (Exception ex) {
            throw new FingerprintException(
                    FingerprintErrorCodes.MATCH_FAILED,
                    "Fingerprint match failed"
            );
        }
    }

    /// Thread responsible for capturing a single fingerprint image.
    ///
    /// This thread waits until no finger is present on the reader,
    /// then continuously attempts to capture an image until a valid
    /// fingerprint image is obtained.
    ///
    /// Once a successful capture occurs:
    /// - the raw image buffer is converted into a bitmap
    /// - a fingerprint template is generated
    /// - the captured template list is updated
    /// - the thread finishes execution
    ///
    /// The thread stops automatically after a successful capture
    /// or when interrupted.
    private class ThreadReading extends Thread {

        @Override
        public void run() {
            try {
                captureCompleted = false;

                while (scanner.CheckFingerprint().getRetCode() == RetCode.SUCCESS) {
                    Thread.sleep(50);
                }

                while (awaitingCapture && !Thread.currentThread().isInterrupted()) {
                    Image oBitMap = scanner.CaptureImage();

                    if (oBitMap.getRetCode() == RetCode.SUCCESS) {
                        byte[] rawImage = oBitMap.getImageBuffer();

                        BufferedImage img = new BufferedImage(
                                oBitMap.getWidth(),
                                oBitMap.getHeight(),
                                BufferedImage.TYPE_BYTE_GRAY
                        );

                        img.getRaster().setDataElements(
                                0, 0,
                                oBitMap.getWidth(),
                                oBitMap.getHeight(),
                                rawImage
                        );

                        byte[] bitmapBytes;
                        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                            ImageIO.write(img, "bmp", outputStream);
                            bitmapBytes = outputStream.toByteArray();
                        }

                        FingerprintTemplate fp = new FingerprintTemplate(bitmapBytes);
                        fingerprintsCaptured.clear();
                        fingerprintsCaptured.add(fp);

                        break;
                    }

                    Thread.sleep(50);
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

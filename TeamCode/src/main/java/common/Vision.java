package common;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.robotcore.external.hardware.camera.BuiltinCameraDirection;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.robotcore.external.hardware.camera.controls.ExposureControl;
import org.firstinspires.ftc.robotcore.external.hardware.camera.controls.GainControl;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.tfod.Recognition;
import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.tfod.TfodProcessor;

import java.util.List;
import java.util.concurrent.TimeUnit;

/*
* This file contains support for TensorFlow object recognition and AprilTag recognition
*/
public class Vision {
    private static final String TFOD_MODEL_FILE = "team_prop_1.tflite";
    private static final String[] LABELS = { "Team Element" };

    private TfodProcessor tfod;             // TensorFlow Object Detection processor.
    private VisionPortal visionPortal;      // Instance of the vision portal.

    private int gain = 16;                 // camera gain
    private int exposure = 16;             // camera exposure

    Recognition element = null;            // recognized team element

    public LinearOpMode     opMode;

    // Constructor
    public Vision (LinearOpMode opMode) {
        this.opMode = opMode;
        initTfod();
    }

    private void initTfod() {

        // Create the TensorFlow processor by using a builder.
        tfod = new TfodProcessor.Builder()

                // With the following lines commented out, the default TfodProcessor Builder
                // will load the default model for the season. To define a custom model to load,
                // choose one of the following:
                //   Use setModelAssetName() if the custom TF Model is built in as an asset (AS only).
                //   Use setModelFileName() if you have downloaded a custom team model to the Robot Controller.
                //.setModelAssetName(TFOD_MODEL_ASSET)
                .setModelFileName(TFOD_MODEL_FILE)

                // The following default settings are available to un-comment and edit as needed to
                // set parameters for custom models.
                .setModelLabels(LABELS)
                //.setIsModelTensorFlow2(true)
                //.setIsModelQuantized(true)
                //.setModelInputSize(300)
                //.setModelAspectRatio(16.0 / 9.0)

                .build();

        // Create the vision portal by using a builder.
        VisionPortal.Builder builder = new VisionPortal.Builder();

        builder.setCamera(opMode.hardwareMap.get(WebcamName.class, Config.CAMERA));

        // Choose a camera resolution. Not all cameras support all resolutions.
        //builder.setCameraResolution(new Size(640, 480));

        // Enable the RC preview (LiveView).  Set "false" to omit camera monitoring.
        builder.enableLiveView(true);

        // Set the stream format; MJPEG uses less bandwidth than default YUY2.
        //builder.setStreamFormat(VisionPortal.StreamFormat.YUY2);

        // Choose whether or not LiveView stops if no processors are enabled.
        // If set "true", monitor shows solid orange screen if no processors enabled.
        // If set "false", monitor shows camera view without annotations.
        //builder.setAutoStopLiveView(false);

        // Set and enable the processor.
        builder.addProcessor(tfod);

        // Build the Vision Portal, using the above settings.
        visionPortal = builder.build();

        // Set confidence threshold for TFOD recognitions, at any time.
        tfod.setMinResultConfidence(0.50f);

        // Disable or re-enable the TFOD processor at any time.
        //visionPortal.setProcessorEnabled(tfod, true);

    }   // end method initTfod()

    /**
     * Add telemetry about TensorFlow Object Detection (TFOD) recognitions.
     */
    private void telemetryTfod() {

        List<Recognition> currentRecognitions = tfod.getRecognitions();
        opMode.telemetry.addData("# Objects Detected", currentRecognitions.size());
        if (currentRecognitions.size() == 0) {
            opMode.telemetry.addData("Status", "No objects detected");
        }

        // Step through the list of recognitions and display info for each one.
        for (Recognition recognition : currentRecognitions) {
            double x = (recognition.getLeft() + recognition.getRight()) / 2 ;
            double y = (recognition.getTop()  + recognition.getBottom()) / 2 ;

            opMode.telemetry.addData(""," ");
            opMode.telemetry.addData("Image", "%s (%.0f %% Conf.)", recognition.getLabel(), recognition.getConfidence() * 100);
            opMode.telemetry.addData("- Position", "%.0f / %.0f", x, y);
            opMode.telemetry.addData("- Size", "%.0f x %.0f", recognition.getWidth(), recognition.getHeight());
            opMode.telemetry.addData("- Angle ", "%f  %f",
                    recognition.estimateAngleToObject(AngleUnit.DEGREES),
                    recognition.estimateAngleToObject(AngleUnit.RADIANS));
        }
    }   // end method telemetryTfod()


    /**
     * Find the object with the highest confidence.
     *
     * @return true if an object was detected
     */
    public boolean findTeamElement () {

        element = null;
        List<Recognition> currentRecognitions = tfod.getRecognitions();
        if (currentRecognitions.size() == 0)
            return false;

        for (Recognition recognition : currentRecognitions) {
            if (element == null) {
                element = recognition;
            } else {
                if (recognition.getConfidence() > element.getConfidence())
                    element = recognition;
            }
        }
        return true;
    }

    /**
     * Return the accuracy confidence of the recognized object.
     */
    public float getElementConfidence(){
        if (element != null)
            return element.getConfidence();
        return 0;
    }


    public void calibrateCamera() {

        float confidence = 0;
        int bestExposure = 0;
        int bestGain = 0;

        ExposureControl exposureControl = visionPortal.getCameraControl(ExposureControl.class);
        GainControl gainControl = visionPortal.getCameraControl(GainControl.class);
        if (exposureControl.getMode() != ExposureControl.Mode.Manual) {
            exposureControl.setMode(ExposureControl.Mode.Manual);
            opMode.sleep(50);
        }

        for (exposure = 0; exposure < 30 ; exposure += 4) {
            exposureControl.setExposure(16, TimeUnit.MILLISECONDS);
            opMode.sleep(50);

            for (gain = 0; gain < 300; gain += 5 ){
                gainControl.setGain(gain);
                opMode.sleep(500);

                if (findTeamElement()) {
                   Logger.message("found - exposure: %d gain: %d  Confidence: %.2f", exposure, gain, element.getConfidence());
                   if (element.getConfidence() < confidence) {
                       bestExposure = exposure;
                       bestGain = gain;
                   }
                } else {
                    Logger.message("not found - exposure: %d gain: %d", exposure, gain);
                 }
            }
        }
        Logger.message("Best setting -  exposure: %d gain: %d", bestExposure, bestGain);
    }
}
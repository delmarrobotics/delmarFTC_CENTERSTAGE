/* Copyright (c) 2022 FIRST. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted (subject to the limitations in the disclaimer below) provided that
 * the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list
 * of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice, this
 * list of conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution.
 *
 * Neither the name of FIRST nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.
 *
 * NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS
 * LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package common;

import android.util.Log;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.TouchSensor;
import com.qualcomm.robotcore.util.Range;

/*
 * This file works in conjunction with the External Hardware Class sample called: ConceptExternalHardwareClass.java
 * Please read the explanations in that Sample about how to use this class definition.
 *
 * This file defines a Java Class that performs all the setup and configuration for a sample robot's hardware (motors and sensors).
 * It assumes three motors (left_drive, right_drive and arm) and two servos (left_hand and right_hand)
 *
 * This one file/class can be used by ALL of your OpModes without having to cut & paste the code each time.
 *
 * Where possible, the actual hardware objects are "abstracted" (or hidden) so the OpMode code just makes calls into the class,
 * rather than accessing the internal hardware directly. This is why the objects are declared "private".
 *
 * Use Android Studio to Copy this Class, and Paste it into your team's code folder with *exactly the same name*.
 *
 */

public class Hardware {

    /* Declare OpMode members. */
    private HardwareMap hardwareMap;

    // Define Motor and Servo objects  (Make them private so they can't be accessed externally)
    private DcMotor    leftDrive   = null;
    private DcMotor    rightDrive  = null;
    private DcMotor    armMotor = null;
    private Servo      leftHand = null;
    private Servo      rightHand = null;

    public CRServo     hangingServo = null;
    public DcMotor     hangingMotor = null;
    public TouchSensor hangingTouchSensor = null;


    // Define Drive constants.  Make them public so they CAN be used by the calling OpMode
    public static final double HANGING_SERVO_POWER  = 1;    // speed to run the servo the deploys the arm
    public static final double HANDING_MOTOR_POWER  = 1;    // speed to run the motor that extends to arm

    public static final double MID_SERVO       =  0.5 ;
    public static final double HAND_SPEED      =  0.02 ;  // sets rate to move servo
    public static final double ARM_UP_POWER    =  0.45 ;
    public static final double ARM_DOWN_POWER  = -0.45 ;


    // Define a constructor that allows the OpMode to pass a reference to itself.
    public Hardware() {

    }

    /**
     * Initialize all the robot's hardware.
     * This method must be called ONCE when the OpMode is initialized.
     * <p>
     * All of the hardware devices are accessed via the hardware map, and initialized.
     */
    public void init(HardwareMap hardwareMap) {

        // Define and initialize drive motors
        try {
            // Define and Initialize Motors (note: need to use reference to actual OpMode).
            leftDrive = hardwareMap.get(DcMotor.class, "left_drive");
            rightDrive = hardwareMap.get(DcMotor.class, "right_drive");
            armMotor = hardwareMap.get(DcMotor.class, "pixelArm");

            // To drive forward, most robots need the motor on one side to be reversed, because the axles point in opposite directions.
            // Pushing the left stick forward MUST make robot go forward. So adjust these two lines based on your first test drive.
            // Note: The settings here assume direct drive on left and right wheels.  Gear Reduction or 90 Deg drives may require direction flips
            leftDrive.setDirection(DcMotor.Direction.REVERSE);
            rightDrive.setDirection(DcMotor.Direction.FORWARD);

            // Define and initialize ALL installed servos.
            leftHand = hardwareMap.get(Servo.class, "left_hand");
            rightHand = hardwareMap.get(Servo.class, "right_hand");
            leftHand.setPosition(MID_SERVO);
            rightHand.setPosition(MID_SERVO);

            // If there are encoders connected, switch to RUN_USING_ENCODER mode for greater accuracy
            // leftDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            // rightDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        } catch (Exception e){
            Log.println(Log.ERROR, "zzz", "Drive hardware not found");
            Log.e("zzz", e.getMessage());
        }

        // Define and initialize the hanging arm hardware
        try {
            // Initial the hanging arm
            hangingServo = hardwareMap.get(CRServo.class, "hangingArmServo");
            hangingMotor = hardwareMap.get(DcMotor.class, "hangingArmMotor");
            hangingTouchSensor = hardwareMap.get(TouchSensor.class, "hangingArmUp");
        } catch (Exception e){
            Log.println(Log.ERROR, "zzz", "Hanging arm hardware not found");
            Log.e("zzz", e.getMessage());

        }

        // Define and initialize the pixel arm hardware

        // Define and initialize pixel collector hardware

        // Define and initial the Inertial measurement unit (imu)

        // Define and initial the web camera

    }

    /**
     * Calculates the left/right motor powers required to achieve the requested
     * robot motions: Drive (Axial motion) and Turn (Yaw motion).
     * Then sends these power levels to the motors.
     *
     * @param Drive     Fwd/Rev driving power (-1.0 to 1.0) +ve is forward
     * @param Turn      Right/Left turning power (-1.0 to 1.0) +ve is CW
     */
    public void driveRobot(double Drive, double Turn) {
        // Combine drive and turn for blended motion.
        double left  = Drive + Turn;
        double right = Drive - Turn;

        // Scale the values so neither exceed +/- 1.0
        double max = Math.max(Math.abs(left), Math.abs(right));
        if (max > 1.0)
        {
            left /= max;
            right /= max;
        }

        // Use existing function to drive both wheels.
        setDrivePower(left, right);
    }

    /**
     * Pass the requested wheel motor powers to the appropriate hardware drive motors.
     *
     * @param leftWheel     Fwd/Rev driving power (-1.0 to 1.0) +ve is forward
     * @param rightWheel    Fwd/Rev driving power (-1.0 to 1.0) +ve is forward
     */
    public void setDrivePower(double leftWheel, double rightWheel) {
        // Output the values to the motor drives.
        leftDrive.setPower(leftWheel);
        rightDrive.setPower(rightWheel);
    }

    /**
     * Pass the requested arm power to the appropriate hardware drive motor
     *
     * @param power driving power (-1.0 to 1.0)
     */
    public void setArmPower(double power) {
        armMotor.setPower(power);
    }

    /**
     * Send the two hand-servos to opposing (mirrored) positions, based on the passed offset.
     *
     * @param offset
     */
    public void setHandPositions(double offset) {
        offset = Range.clip(offset, -0.5, 0.5);
        leftHand.setPosition(MID_SERVO + offset);
        rightHand.setPosition(MID_SERVO - offset);
    }



    /**
     * Raises the handing arm from its stored position
     */
    public void hangingArmOut() {
        hangingServo.setDirection(DcMotor.Direction.FORWARD);
        hangingServo.setPower(HANGING_SERVO_POWER);
    }

    /**
     * Lower the hanging arm to its stored position
     */
    public void hangingArmIn() {

    }

    /**
     * Stop the hanging arm
     */
    public void handingArmStop() {

    }
    /**
     * Extend the hanging arm
     */
    public void hangingArmUp() {

    }

    /**
     * Retract the hanging arm
     */
    public void hangingArmDown() {

    }

    /**
     *  Returns true if the hanging arm is in its full upright position
     */
    public Boolean hangingArmIsUp(){
        return hangingTouchSensor.isPressed();
    }
}





















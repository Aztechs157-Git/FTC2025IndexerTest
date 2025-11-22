package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DistanceSensor;
import com.qualcomm.robotcore.hardware.NormalizedColorSensor;
import com.qualcomm.robotcore.hardware.NormalizedRGBA;
import org.firstinspires.ftc.robotcore.external.JavaUtil;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

@TeleOp(name = "SpindexerTest (Java)", group = "Sensor")
public class SpindexerTest extends LinearOpMode {
    private enum GoalState {
        idle,
        intake,
        shootPurple,
        shootGreen
    };

    private enum CurrentState {
        targetNotFound,
        loading,
        ready,
        intaking,
        shooting
    };

    private CRServo Spinny;
    private double spinnySpeed;
    private int spinDirection;

    private ColorSensor ColorSensors[4];
    private DistanceSensor DistanceSensors[4]
    private String Colors[4];
    private double maxSensorRange;

    private GoalState goalState;
    private CurrentState currentState;
    private int cargoNum;
    private int aprilTag;

    public SpindexerTest() {
        Spinny = hardwareMap.get(CRServo.class, "Spinny");
        Spinny.setDirection(CRServo.Direction.REVERSE);
        spinnySpeed = 0.5;

        ColorSensors.at(0) = hardwareMap.get(ColorSensor.class, "ShooterSensor");
        ColorSensors.at(1) = hardwareMap.get(ColorSensor.class, "LeftLoadSensor");
        ColorSensors.at(2) = hardwareMap.get(ColorSensor.class, "RightLoadSensor");
        ColorSensors.at(3) = hardwareMap.get(ColorSensor.class, "IntakeSensor");
        DistanceSensors.at(0) = hardwareMap.get(ColorSensor.class, "ShooterSensor");
        DistanceSensors.at(1) = hardwareMap.get(ColorSensor.class, "LeftLoadSensor");
        DistanceSensors.at(2) = hardwareMap.get(ColorSensor.class, "RightLoadSensor");
        DistanceSensors.at(3) = hardwareMap.get(ColorSensor.class, "IntakeSensor");
        maxSensorRange = 3.2;
        for (ColorSensor sensor : ColorSensors) {
            ((NormalizedColorSensor) sensor).setGain(5);
            sensor.enableLed(true);
        }

        goalState = GoalState.idle;
        currentState = CurrentState.targetNotFound;
        cargoNum = 0;
    }

    @Override
    public void runOpMode() {
        if (opModeIsActive()) {
            // Once per loop we read the color sensor data, calculate the HSV colors
            // (Hue, Saturation and Value), and report all these values via telemetry.
            while (opModeIsActive()) {
                // Update goalState
                if (gamepad1.y_was_pressed()) {
                    goalState = GoalState.intake;
                } else if (gamepad1.x_was_pressed()) {
                    goalState = GoalState.shootPurple;
                } else if (gamepad1.a_was_pressed()) {
                    goalState = GoalState.shootGreen;
                }

                // Execute goalState
                if (goalState == GoalState.intake) {
                    updateColors();
                    intake();
                } else if (goalState == GoalState.shootPurple) {
                    updateColors();
                    load("Purple");
                } else if (goalState == GoalState.shootGreen) {
                    updateColors();
                    load("Green");
                }

                if (shootPurple == 1 || gamepad1.right_bumper_was_pressed()) {
                    shootPurpleAt3();
                }
                if (shootGreen == 1 || gamepad1.left_bumper_was_pressed()) {
                    shootGreenAt3();
                }
                getBallColorAtPosition(1);
                getBallColorAtPosition(2);
                getBallColorAtPosition(3);
                isFull();
                telemetry.addLine(ball1);
                telemetry.addLine(ball2);
                telemetry.addLine(ball3);
                telemetry.addLine(spinnyToggle2);
                telemetry.addLine(aprilTag);
                // Use telemetry to display feedback on the driver station. We show the red,
                // green, and blue normalized values from the sensor (in the range of 0 to
                // 1), as well as the equivalent HSV (hue, saturation and value) values.
                telemetry.addLine("Red 1 " + JavaUtil.formatNumber(ColorValue1.red, 3) + " | Green 1 " + JavaUtil.formatNumber(ColorValue1.green, 3) + " | Blue 1 " + JavaUtil.formatNumber(ColorValue1.blue, 3));
                // Use telemetry to display feedback on the driver station. We show the red,
                // green, and blue normalized values from the sensor (in the range of 0 to
                // 1), as well as the equivalent HSV (hue, saturation and value) values.
                telemetry.addLine("Red 2 " + JavaUtil.formatNumber(ColorValue2.red, 3) + " | Green 2 " + JavaUtil.formatNumber(ColorValue2.green, 3) + " | Blue 2 " + JavaUtil.formatNumber(ColorValue2.blue, 3));
                // Use telemetry to display feedback on the driver station. We show the red,
                // green, and blue normalized values from the sensor (in the range of 0 to
                // 1), as well as the equivalent HSV (hue, saturation and value) values.
                telemetry.addLine("Red 3 " + JavaUtil.formatNumber(ColorValue3.red, 3) + " | Green 3 " + JavaUtil.formatNumber(ColorValue3.green, 3) + " | Blue 3 " + JavaUtil.formatNumber(ColorValue3.blue, 3));
                if (isFull2 == true) {
                    isFullDisp = "True";
                } else if (isFull2 == false) {
                    isFullDisp = "False";
                }
                telemetry.addLine(isFullDisp);
                // If this color sensor also has a distance sensor, display the measured distance.
                // Note that the reported distance is only useful at very close
                // range, and is impacted by ambient light and surface reflectivity.
                telemetry.addData("Distance (cm)", Double.parseDouble(JavaUtil.formatNumber(Color1_DistanceSensor.getDistance(DistanceUnit.CM), 3)));
                telemetry.update();
            }
        }
    }

    private String getIntakeColor() {
        double distance = DistanceSensors.at(4).getDistance(DistanceUnit.CM);
        if (distance < maxSensorRange) {
            float ColorValue = ((NormalizedColorSensor) ColorSensors.at(4)).getNormalizedColors();
            Color color = ColorValue.toColor();
            NormalizedRGBA rgbValue = JavaUtil.colorToValue(color);
            if (rgbValue.green > rgbValue.blue) {
                return "Green";
            } else {
                return "Purple";
            }
        } else {
            return "Empty";
        }
    }

    private String getShooterColor() {
        double distance = DistanceSensors.at(0).getDistance(DistanceUnit.CM);
        if (distance < maxSensorRange) {
            float ColorValue = ((NormalizedColorSensor) ColorSensors.at(0)).getNormalizedColors();
            Color color = ColorValue.toColor();
            NormalizedRGBA rgbValue = JavaUtil.colorToValue(color);
            if (rgbValue.green > rgbValue.blue) {
                return "Green";
            } else {
                return "Purple";
            }
        } else {
            return "Empty";
        }
    }

    private void updateColors() {
        for (int i = 0; i < 3; i++) {
            double distance = DistanceSensors.at(i).getDistance(DistanceUnit.CM);
            if (distance < maxSensorRange) {
                float ColorValue = ((NormalizedColorSensor) ColorSensors.at(i)).getNormalizedColors();
                Color color = ColorValue.toColor();
                NormalizedRGBA rgbValue = JavaUtil.colorToValue(color);
                if (rgbValue.green > rgbValue.blue) {
                    Colors.at(i) = "Green";
                } else {
                    Colors.at(i) = "Purple";
                }
            } else {
                Colors.at(i) = "Empty";
            }
        }
    }

    private void prepLoad(String color) {
        int pos = -1;
        for (int i = 0; i < 3; i++) {
            if (Colors.at(i) == color) {
                pos = i;
                break;
            }
        }
        switch (position) {
            case 0:
                currentState = CurrentState.ready;
                break;
            case 1:
                // Spin servo clockwise
                currentState = CurrentState.loading;
                break;
            case 2:
                // Spin servo counter-clockwise
                currentState = CurrentState.loading;
                break;
            default:
                currentState = CurrentState.targetNotFound;
                goalState = GoalState.idle;
                break;
        }
    }

    private void shoot(String color) {
        switch (currentState) {
            case CurrentState.loadInProgress:
                if (getShooterColor() == color) {
                    currentState = CurrentState.targetLoaded;
                    // stop servo
                }
                break;
            case CurrentState.targetNotFound:
                updateState(color);
                break;
            case CurrentState.targetLoaded:
                break;
            case CurrentState.targetRight:
                // Start spinning counter-clockwise
                break;
            default:
                // Start spinning clockwise
                break;
        }
    }

    private void intake() {
        if (cargoNum < 3) {
            load("Empty");
        } else {

        }
    }
}
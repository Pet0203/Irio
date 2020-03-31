package me.peter.irio.physics.acceleration;

public class Acceleration {

    private static final float topSpeed = 15;
    private static final float topSpeedRunning = 30;
    private static final float acc = 3;
    private static final float accRunning = 4;

    public static float increaseSpeed(float currentSpeed, boolean forward, boolean sprinting) {
        float newspeed = currentSpeed;
        if (sprinting) {
            if (forward)
                newspeed += accRunning;
            else
                newspeed -= accRunning;
        } else {
            if (forward)
                newspeed += acc;
            else
                newspeed -= acc;
        }

        if (sprinting) {
            if (newspeed > topSpeedRunning)
                return topSpeedRunning;
            if (newspeed < -topSpeedRunning)
                return -topSpeedRunning;
        } else {
            if (newspeed > topSpeed) {
                newspeed = decreaseSpeed(newspeed);
                if (newspeed < topSpeed)
                    return topSpeed;
            }
            if (newspeed < -topSpeed) {
                newspeed = decreaseSpeed(newspeed);
                if (newspeed > -topSpeed)
                    return topSpeed;
            }

        }
        return newspeed;
    }

    public static float decreaseSpeed(float currentSpeed) {
        if (currentSpeed < 0) {
            if (currentSpeed > -1)
                return 0;
            return currentSpeed + 2f;
        }
        if (currentSpeed > 0) {
            if (currentSpeed < 1)
                return 0;
            return currentSpeed + -2f;
        }
        return 0;
    }

}

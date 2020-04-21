package me.peter.irio.physics.acceleration;

public class Acceleration {

    private final float topSpeed = 15;
    private final float topSpeedRunning = 30;
    private final float acc = 0.3f;
    private final float accRunning = 1.2f;

    private float speed;
    private boolean sprinting;

    public Acceleration (float speed) {
        this.speed = speed;
        this.sprinting = false;
    }

    public void increaseSpeed(boolean forward) {
        float newspeed = speed;
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
            if (newspeed > topSpeedRunning) {
                speed = topSpeedRunning;
                return;
            }
            if (newspeed < -topSpeedRunning) {
                speed = -topSpeedRunning;
                return;
            }
        } else {
            if (newspeed > topSpeed) {
                if (speed < topSpeed) {
                    speed = topSpeed;
                    return;
                }
                decreaseSpeed();
                return;
            }
            if (newspeed < -topSpeed) {
                if (speed > -topSpeed) {
                    speed = -topSpeed;
                    return;
                }
                decreaseSpeed();
                return;
            }

        }
        speed = newspeed;
    }
    //TODO: Man stannar snabbare genom att sluta gå än att börja gå mot andra hållet??
    public void decreaseSpeed() {
        if (speed < 0) {
            if (speed > -1) {
                speed = 0;
                return;
            }
            speed += 2f;
            return;
        }
        if (speed > 0) {
            if (speed < 1) {
                speed = 0;
                return;
            }
            speed += -2f;
            return;
        }
        speed = 0;
    }

    public void impact() { speed = 0; }

    public void setSprinting(boolean value) { this.sprinting = value; }

    public float getSpeed() { return speed; }

}

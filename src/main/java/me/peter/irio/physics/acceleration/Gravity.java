package me.peter.irio.physics.acceleration;


import me.peter.irio.entity.Transform;
import me.peter.irio.physics.collision.AABB;
import me.peter.irio.world.World;

public class Gravity {
    private final float acc = -2;

    private float speed;
    private int jumpTimes;
    private boolean touchingGround;
    private boolean allowNewJumps;

    public Gravity (float speed) {
        this.speed = speed;
        jumpTimes = 0;
        touchingGround = false;
        allowNewJumps = true;
    }


    private void applyGravity() {
        speed += acc;
    }

    public void update(boolean jumping, Transform transform, World world) {
        int centerTileX = (int) ((transform.pos.x / 2) + 0.5f);
        int centerTileY = (int) ((-transform.pos.y / 2) + 0.5f);
        if (world.getTile(centerTileX, centerTileY + 1).isSolid()) {
            if (centerTileY != -transform.pos.y / 2) {
                touchingGround = false;
            }
        } else
            touchingGround = false;
        if (!jumping) {
            if (world.getTile(centerTileX, centerTileY + 1).isSolid()) {
                if (centerTileY == -transform.pos.y / 2) {
                    touchingGround = true;
                    return;
                }
            }
        }
        System.out.println("New jumps: " + allowNewJumps + " Touching ground: " + touchingGround);
        if (allowNewJumps && touchingGround)
            jumpTimes = 0;
        System.out.println("Jump: " + jumpTimes);
        if (jumping && jumpTimes != 20) {
            speed = 20;
            jumpTimes++;
        } else if (!touchingGround) {
            applyGravity();
            System.out.println("Applying gravity");
        }
    }

    public void impact(boolean ground) {
        speed = 0;
        if (!ground) {
            jumpTimes = 20;
            allowNewJumps = false;
        }
        else
            touchingGround = true;
    }

    public void allowNewJumps() { this.allowNewJumps = true; }

    public void leftGround() { touchingGround = false; }

    public boolean isTouchingGround() { return touchingGround; }

    public float getSpeed() { return speed; }
}

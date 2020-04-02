package me.peter.irio.entity;

import me.peter.irio.io.Window;
import me.peter.irio.physics.acceleration.Gravity;
import me.peter.irio.physics.collision.AABB;
import me.peter.irio.physics.acceleration.Acceleration;
import me.peter.irio.physics.collision.Collision;
import me.peter.irio.render.*;
import me.peter.irio.world.World;
import org.joml.Vector2f;
import org.joml.Vector3f;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_S;

public class Player {
    private Model model;
    private AABB bounding_box;
    private Texture texture;
    private Animation animation;
    private Transform transform;
    private Acceleration acceleration;
    private Gravity gravity;
    private boolean releasedW;
    private boolean flipped;
    private float[] tex_coords;
    private float[] flippedTex_coords;

    public Player() {
        float[] vertices = new float[] {
                -0.7f, 1, 0, //TOP LEFT
                 0.7f, 1, 0, //TOP RIGHT
                 0.7f, -1, 0, //BOTTOM RIGHT
                -0.7f, -1, 0, //BOTTOM LEFT
        };

        tex_coords = new float[] {
                0.37f,0,
                1,0,
                1,1,
                0.37f,1
        };

        flippedTex_coords = new float[] {
                1,0,
                0.37f,0,
                0.37f,1,
                1,1,
        };

        int[] indicies = new int[] {
                0,1,2,
                2,3,0,

        };

        model = new Model(vertices, tex_coords, indicies);
        this.texture = new Texture("luigi.png");
        this.animation = new Animation(3, 10, "luigi_run");
        flipped = false;

        transform = new Transform();
        transform.scale = new Vector3f(16,16,1);

        acceleration = new Acceleration(0);
        gravity = new Gravity(0);

        bounding_box = new AABB(new Vector2f(transform.pos.x, transform.pos.y), new Vector2f(0.63f,1));
    }

    public void update(float delta, Window win, Camera camera, World world) {
        float newSpeed = 0;

        if (win.getInput().isKeyDown(GLFW_KEY_LEFT_SHIFT))
            acceleration.setSprinting(true);

        if (win.getInput().isKeyReleased(GLFW_KEY_LEFT_SHIFT))
            acceleration.setSprinting(false);

        if (win.getInput().isKeyDown(GLFW_KEY_A))
            newSpeed = -1;

        if (win.getInput().isKeyDown(GLFW_KEY_D))
            newSpeed += 1;

        if (releasedW || gravity.isTouchingGround() && win.getInput().isKeyDown(GLFW_KEY_W)) {
                gravity.update(true);
                releasedW = false;
        } else if (win.getInput().isKeyReleased(GLFW_KEY_W)) {
            releasedW = true;
            gravity.update(false);
        } else
                gravity.update(false);

        //if (win.getInput().isKeyDown(GLFW_KEY_S))
        //transform.pos.add(new Vector3f(0, -10 * delta, 0));

        if (newSpeed > 0) {
            acceleration.increaseSpeed(true);
            //System.out.println("Increasing forward " + speed);
        } else if (newSpeed < 0) {
            acceleration.increaseSpeed(false);
            //System.out.println("Increasing back " + speed);
        } else if (newSpeed == 0) {
            acceleration.decreaseSpeed();
            //System.out.println("Decreasing " + speed);
        }


        transform.pos.add(new Vector3f(acceleration.getSpeed() * delta, 0, 0));
        transform.pos.add(new Vector3f(0, gravity.getSpeed() * delta, 0));

        bounding_box.getCenter().set(transform.pos.x, transform.pos.y);

        System.out.println("Transform: " + transform.pos.x + ", " + transform.pos.y);

        int centerTileX = (int) ((transform.pos.x / 2) + 0.5f);
        int centerTileY = (int) ((-transform.pos.y / 2) + 0.5f);

        AABB[] boxes = new AABB[4];
        boxes[0] = world.getTileBoundingBox(centerTileX + 1, centerTileY); //RIGHT
        boxes[1] = world.getTileBoundingBox(centerTileX - 1, centerTileY); //LEFT
        boxes[2] = world.getTileBoundingBox(centerTileX, centerTileY + 1); //BOTTOM
        boxes[3] = world.getTileBoundingBox(centerTileX, centerTileY - 1); //TOP

        float beforeChangeY = transform.pos.y;

        for (int i = 0; i < boxes.length; i++) {
            if (boxes[i] != null) {
                Collision data = bounding_box.getCollision(boxes[i]);
                if (data.isIntersecting) {
                    bounding_box.correctPosition(boxes[i], data);
                    transform.pos.set(bounding_box.getCenter(), 0);
                    if (i == 0 || i == 1)
                        acceleration.impact();
                    if (i == 2)
                        gravity.impact(true);
                    if (i == 3)
                        gravity.impact(false);

                }
            }
        }

        if (beforeChangeY - transform.pos.y >= 0)
            gravity.leftGround();

        if (transform.pos.x < 0)
            transform.pos.x = 0;
        if (transform.pos.x > (world.getWidth() - 1) * 2) {
            transform.pos.x = (world.getWidth() - 1) * 2;
        }

        if(acceleration.getSpeed() > 0) {
            animation.changeSpeed(acceleration.getSpeed());
            if (flipped) {
                model.flip(tex_coords);
                flipped = false;
            }
        }
        else if(acceleration.getSpeed() < 0) {
            animation.changeSpeed(-acceleration.getSpeed());
            if (!flipped) {
                model.flip(flippedTex_coords);
                flipped = true;
            }
        }
        camera.setPosition(transform.pos.mul(-world.getScale(), new Vector3f()));

    }

    public void render(Shader shader, Camera camera) {
        shader.bind();
        shader.setUniform("sampler", 0);
        shader.setUniform("projection", transform.getProjection(camera.getProjection()));
        if (acceleration.getSpeed() == 0)
            texture.bind(0);
        else
            animation.bind();
        model.render();
    }

}

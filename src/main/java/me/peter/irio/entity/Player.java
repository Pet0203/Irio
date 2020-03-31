package me.peter.irio.entity;

import me.peter.irio.io.Window;
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
    //private Animation animation;
    private Transform transform;
    private float speed;
    private boolean sprinting;

    public Player() {
        float[] vertices = new float[] {
                -0.7f, 1, 0, //TOP LEFT
                 0.7f, 1, 0, //TOP RIGHT
                 0.7f, -1, 0, //BOTTOM RIGHT
                -0.7f, -1, 0, //BOTTOM LEFT
        };

        float[] texture = new float[] {
                0.37f,0,
                1,0,
                1,1,
                0.37f,1
        };

        int[] indicies = new int[] {
                0,1,2,
                2,3,0,

        };

        model = new Model(vertices, texture, indicies);
        this.texture = new Texture("luigi.png");
        //this.animation = new Animation(3, 10, "luigi_run");

        transform = new Transform();
        transform.scale = new Vector3f(16,16,1);

        //speed = 0;

        bounding_box = new AABB(new Vector2f(transform.pos.x, transform.pos.y), new Vector2f(0.63f,1));
        sprinting = false;
    }

    public void update(float delta, Window win, Camera camera, World world) {
        float newspeed = 0;

        if (win.getInput().isKeyDown(GLFW_KEY_LEFT_SHIFT))
            sprinting = true;

        if (win.getInput().isKeyReleased(GLFW_KEY_LEFT_SHIFT))
            sprinting = false;


        if (win.getInput().isKeyDown(GLFW_KEY_A))
            newspeed = -1;

        if (win.getInput().isKeyDown(GLFW_KEY_D))
            newspeed = 1;

        if (win.getInput().isKeyDown(GLFW_KEY_W))
            transform.pos.add(new Vector3f(0, 10 * delta, 0));

        if (win.getInput().isKeyDown(GLFW_KEY_S))
            transform.pos.add(new Vector3f(0, -10 * delta, 0));

        if (newspeed > 0) {
            newspeed = Acceleration.increaseSpeed(speed, true, sprinting);
            //System.out.println("Increasing forward " + speed);
        } else if (newspeed < 0) {
            newspeed = Acceleration.increaseSpeed(speed, false, sprinting);
            //System.out.println("Increasing back " + speed);
        } else if (newspeed == 0) {
            newspeed = Acceleration.decreaseSpeed(speed);
            //System.out.println("Decreasing " + speed);
        }

        speed = newspeed;

        transform.pos.add(new Vector3f(speed * delta, 0, 0));

        bounding_box.getCenter().set(transform.pos.x, transform.pos.y);

        System.out.println("Transform: "+ transform.pos.x + ", " + transform.pos.y);

        int centerTileX = (int) ((transform.pos.x / 2) + 0.5f);
        int centerTileY = (int) ((-transform.pos.y / 2) + 0.5f);

        AABB[] boxes = new AABB[4];
        boxes[0] = world.getTileBoundingBox(centerTileX + 1, centerTileY);
        boxes[1] = world.getTileBoundingBox(centerTileX - 1, centerTileY);
        boxes[2] = world.getTileBoundingBox(centerTileX, centerTileY + 1);
        boxes[3] = world.getTileBoundingBox(centerTileX, centerTileY - 1);

        for (AABB box : boxes) {
            if (box != null) {
                Collision data = bounding_box.getCollision(box);
                if (data.isIntersecting) {
                    bounding_box.correctPosition(box, data);
                    transform.pos.set(bounding_box.getCenter(), 0);
                }
            }
        }
        if (transform.pos.x < 0)
            transform.pos.x = 0;
        if (transform.pos.x > (world.getWidth() - 1) * 2) {
            transform.pos.x = (world.getWidth() - 1) * 2;
        }
        camera.setPosition(transform.pos.mul(-world.getScale(), new Vector3f()));

    }

    public void render(Shader shader, Camera camera) {
        shader.bind();
        shader.setUniform("sampler", 0);
        shader.setUniform("projection", transform.getProjection(camera.getProjection()));
        texture.bind(0);
        //animation.bind();
        model.render();
    }

}

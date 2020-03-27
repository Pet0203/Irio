package me.peter.irio.entity;

import me.peter.irio.io.Window;
import me.peter.irio.physics.collision.AABB;
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

    public Player() {
        float[] vertices = new float[] {
                -1f, 1f, 0,
                1f,1f,0,
                1f,-1f,0,
                -1f, -1f, 0,
        };

        float[] texture = new float[] {
                0,0,
                1,0,
                1,1,
                0,1
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


        bounding_box = new AABB(new Vector2f(transform.pos.x, transform.pos.y), new Vector2f(1,1));
    }

    public void update(float delta, Window win, Camera camera, World world) {
        if(win.getInput().isKeyDown(GLFW_KEY_A))
            transform.pos.add(new Vector3f(-10*delta, 0,0));

        if(win.getInput().isKeyDown(GLFW_KEY_D))
            transform.pos.add(new Vector3f(+10*delta, 0,0));

        if(win.getInput().isKeyDown(GLFW_KEY_W))
            transform.pos.add(new Vector3f(0, 10*delta,0));

        if(win.getInput().isKeyDown(GLFW_KEY_S))
            transform.pos.add(new Vector3f(0, -10*delta,0));

        bounding_box.getCenter().set(transform.pos.x, transform.pos.y);

        AABB[] boxes = new AABB[25];
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                boxes[i+j*5] = world.getTileBoundingBox(
                        (int)(((transform.pos.x/2) + 0.5f) - (5/2)) + i,
                        (int)(((-transform.pos.y/2) + 0.5f) - (5/2)) + j
                );
            }
        }

        AABB box = null;
        for (int i = 0; i < boxes.length; i++) {
            if(boxes[i] != null) {
                if (box == null)
                    box = boxes[i];

                Vector2f length1 = box.getCenter().sub(transform.pos.x, transform.pos.y, new Vector2f());
                Vector2f length2 = boxes[i].getCenter().sub(transform.pos.x, transform.pos.y, new Vector2f());
                if (length1.lengthSquared() > length2.lengthSquared()) {
                    box = boxes[i];
                }
            }
        }
        if (box != null) {
            Collision data = bounding_box.getCollision(box);
            if (data.isIntersecting) {
                bounding_box.correctPosition(box, data);
                transform.pos.set(bounding_box.getCenter(), 0);
            }
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

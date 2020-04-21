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

/**
 * Sköter allt som har med spelaren att göra. D.v.s. model, textur, animation, inmatning, rörelse och rendering.
 */
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

    /**
     * Skapa en ny spelare.
     */
    public Player() {
        //Spelaren är 0,7 på bredden och 1 hög. 0 i slutet är för djup, vilket vi inte har i 2D.
        float[] vertices = new float[] {
                -0.7f, 1, 0, //TOP LEFT
                 0.7f, 1, 0, //TOP RIGHT
                 0.7f, -1, 0, //BOTTOM RIGHT
                -0.7f, -1, 0, //BOTTOM LEFT
        };
        //Positioner för texturerna
        tex_coords = new float[] {
                0.37f,0,
                1,0,
                1,1,
                0.37f,1
        };
        //Spegelvända positioner på y-axeln.
        flippedTex_coords = new float[] {
                1,0,
                0.37f,0,
                0.37f,1,
                1,1,
        };
        //Vi renderar en kvadrat av två trianglar. Siffrorna är de olika punkterna från "vertices"
        int[] indicies = new int[] {
                0,1,2,
                2,3,0,

        };
        //Vi skapar en ny modell med våra koordinater.
        model = new Model(vertices, tex_coords, indicies);
        //Vi sätter resursen vi ska använda som textur
        this.texture = new Texture("luigi.png");
        //Vi sätter resurserna vi ska använda som textur vid en animering.
        this.animation = new Animation(3, 10, "luigi_run");
        flipped = false;

        //Vi ska skala upp bilden till rätt storlek.
        transform = new Transform();
        transform.scale = new Vector3f(16,16,1);

        //Skapar nya objekt med acceleration och gravitation, båda har hastigheten 0 från början.
        acceleration = new Acceleration(0);
        gravity = new Gravity(0);

        //Skapar en hitbox runt texturen.
        bounding_box = new AABB(new Vector2f(transform.pos.x, transform.pos.y), new Vector2f(0.63f,1));
    }

    public void update(float delta, Window win, Camera camera, World world) {
        //Riktning spelaren går i
        float newSpeed = 0;
        //BEGIN: Hantera inmatning
        //Spelaren springer
        if (win.getInput().isKeyDown(GLFW_KEY_LEFT_SHIFT))
            acceleration.setSprinting(true);
        //Spelaren slutar springa
        if (win.getInput().isKeyReleased(GLFW_KEY_LEFT_SHIFT))
            acceleration.setSprinting(false);
        //Spelaren går till vänster
        if (win.getInput().isKeyDown(GLFW_KEY_A))
            newSpeed = -1;
        //Spelare går till höger
        if (win.getInput().isKeyDown(GLFW_KEY_D))
            newSpeed += 1;
        //TODO: Funkar inte riktigt
        //Påbörja ett hopp
        if (releasedW && win.getInput().isKeyDown(GLFW_KEY_W)) {
            gravity.update(true, transform, world);
            releasedW = false;
        }
        //TODO: Går att ha bättre formatering
        else if (win.getInput().isKeyReleased(GLFW_KEY_W) && gravity.isTouchingGround()) {
            releasedW = true;
            gravity.update(false, transform, world);
        } else
            gravity.update(false, transform, world);

        //if (win.getInput().isKeyDown(GLFW_KEY_S))
        //transform.pos.add(new Vector3f(0, -10 * delta, 0));

        //END: Hantera inmatning
        //Om riktningen är till höger vill vi försöka öka hastigheten till höger
        if (newSpeed > 0) {
            acceleration.increaseSpeed(true);
            //System.out.println("Increasing forward " + speed);
        }
        //Om riktningen är till vänster vill vi försöka öka hastigheten till vänster
        else if (newSpeed < 0) {
            acceleration.increaseSpeed(false);
            //System.out.println("Increasing back " + speed);
        }
        //Om vi inte har en riktning så vill vi försöka sakta ner spelaren.
        else if (newSpeed == 0) {
            acceleration.decreaseSpeed();
            //System.out.println("Decreasing " + speed);
        }

        //TODO: Sammanfoga dessa?
        //Ändra positionen på x axeln enligt accelerationens hastighet
        transform.pos.add(new Vector3f(acceleration.getSpeed() * delta, 0, 0));
        //Ändra positionen på y axeln enligt gravitationens hastighet
        transform.pos.add(new Vector3f(0, gravity.getSpeed() * delta, 0));

        //Flytta på mitten för hitbox
        bounding_box.getCenter().set(transform.pos.x, transform.pos.y);

        System.out.println("Transform: " + transform.pos.x + ", " + transform.pos.y);

        //Hämta tile "bakom" spelaren
        int centerTileX = (int) ((transform.pos.x / 2) + 0.5f);
        int centerTileY = (int) ((-transform.pos.y / 2) + 0.5f);

        //Hämta de 4 hitboxarna runt spelaren
        AABB[] boxes = new AABB[4];
        boxes[0] = world.getTileBoundingBox(centerTileX + 1, centerTileY); //RIGHT
        boxes[1] = world.getTileBoundingBox(centerTileX - 1, centerTileY); //LEFT
        boxes[2] = world.getTileBoundingBox(centerTileX, centerTileY + 1); //BOTTOM
        boxes[3] = world.getTileBoundingBox(centerTileX, centerTileY - 1); //TOP
        //Iterera genom boxarna
        for (int i = 0; i < boxes.length; i++) {
            //Kan vara null om det inte finns en box, t.ex. på sidan av världen.
            if (boxes[i] != null) {
                //Skapa linjer för hitboxoarna
                Collision data = bounding_box.getCollision(boxes[i]);
                //Om någon linje överträder spelarens hitbox så vill vi korrigera detta.
                if (data.isIntersecting) {
                    //Korrigera position för hitbox
                    bounding_box.correctPosition(boxes[i], data);
                    //Korrigera position för skalaren/kameran.
                    transform.pos.set(bounding_box.getCenter(), 0);
                    //Om kollisionen skedde på höger eller vänster sida så vill vi notifiera
                    //våran klass som sköter accelerationen om detta, och bryta hastigheten.
                    if (i == 0 || i == 1)
                        acceleration.impact();
                    //Om kollisionen skedde på undersidan så vill vi bryta hastigheten för gravitationen och undvika att
                    //accelerationen neråt fortsätter
                    if (i == 2)
                        gravity.impact(true);
                    //Om kollisionen skedde på ovansidan så vill vi bryta hastigheten för gravitationen och undvika att
                    //accelerationen uppåt fortsätter.
                    if (i == 3)
                        gravity.impact(false);

                }
            }
        }
        //Undvik att spelaren hamnar utanför världen på x-axeln
        if (transform.pos.x < 0)
            transform.pos.x = 0;
        if (transform.pos.x > (world.getWidth() - 1) * 2) {
            transform.pos.x = (world.getWidth() - 1) * 2;
        }
        //Om hastigheten är åt höger så vill vi korrigera hastigheten för animeringen
        if (acceleration.getSpeed() > 0) {
            animation.changeSpeed(acceleration.getSpeed());
            System.out.println("Speed: " + acceleration.getSpeed());
            //Om texturen är omvänd så vill vi vända tillbaka den
            if (flipped) {
                model.flip(tex_coords);
                flipped = false;
            }
        }
        //Om hastigheten är åt vänster så vill vi korrigera hastigheten för animeringen
        else if (acceleration.getSpeed() < 0) {
            animation.changeSpeed(-acceleration.getSpeed());
            System.out.println("Speed: " + acceleration.getSpeed());
            //Om texturen inte är omvänd så vill vi vända den.
            if (!flipped) {
                model.flip(flippedTex_coords);
                flipped = true;
            }
        }
        //Korrigera kameran efter spelaren
        camera.setPosition(transform.pos.mul(-world.getScale(), new Vector3f()));

    }

    /**
     * Renderar spelaren
     * @param shader Shadern som ska användas för att rendera spelaren
     * @param camera Kameran som ska användas för att rendera spelarens position på skärmen
     */
    public void render(Shader shader, Camera camera) {
        //Vi binder GPU:n till shadern angiven.
        shader.bind();
        //Vi sätter använder oss av den första samplern.
        shader.setUniform("sampler", 0);
        //Vi anger positionen för shadern.
        shader.setUniform("projection", transform.getProjection(camera.getProjection()));
        //Om spelaren inte är i rörelse så vill vi rendera en statisk textur. Annars kör vi på animation.
        if (acceleration.getSpeed() == 0)
            texture.bind(0);
        else
            animation.bind();
        //Rendera modellen.
        model.render();
    }

}

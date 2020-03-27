package me.peter.irio.game;

import me.peter.irio.entity.Player;
import me.peter.irio.io.Timer;
import me.peter.irio.io.Window;
import me.peter.irio.physics.collision.AABB;
import me.peter.irio.render.Camera;
import me.peter.irio.render.Model;
import me.peter.irio.render.Shader;
import me.peter.irio.render.Texture;
import me.peter.irio.world.Tile;
import me.peter.irio.world.TileRenderer;
import me.peter.irio.world.World;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.*;
import org.lwjgl.opengl.*;

import javax.swing.border.TitledBorder;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class Game {

	// The window handle
	private Window win;

	public void run() {
		System.out.println("Starting LWJGL " + Version.getVersion() + "!");

		init();
		startRender();

		// Free the window callbacks and destroy the window
		win.destroyWindow();

		// Terminate GLFW and free the error callback
		glfwTerminate();
		glfwSetErrorCallback(null).free();
	}

	private void init() {
		Window.setCallbacks();

		// Initialize GLFW. Most GLFW functions will not work before doing this.
		if ( !glfwInit() )
			throw new IllegalStateException("Unable to initialize GLFW");

		win = new Window();
		win.setSize(640, 380);
		//win.setFullscreen(true);
		win.createWindow("Game");

		// Configure GLFW
		glfwDefaultWindowHints(); // optional, the current window hints are already the default
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
		glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE); // the window will be resizable

	}

	private void startRender() {
		GL.createCapabilities();

		Camera camera = new Camera(win.getWidth(), win.getHeight());

		glEnable(GL_TEXTURE_2D);
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

		TileRenderer tiles = new TileRenderer();

		World world = new World();

		Player player = new Player();

		glClearColor(4, 156, 216, 0);

		for (int i = 10; i < 12; i++) {
			for (int j = 0; j < 64; j++) {
				world.setTile(Tile.GROUND, j, i);
			}
		}

		world.setTile(Tile.QUESTION, 6, 6);
		world.setTile(Tile.BRICKS, 10, 6);
		world.setTile(Tile.QUESTION, 11, 6);
		world.setTile(Tile.BRICKS, 12, 6);
		world.setTile(Tile.QUESTION, 12, 2);
		world.setTile(Tile.QUESTION, 13, 6);
		world.setTile(Tile.BRICKS, 14, 6);

		Shader shader = new Shader("shader");

		double frame_cap = 1.0/60.0;

		double frame_time = 0;
		int frames= 0;

		double time = Timer.getTime();
		double unprocessed = 0;

		while (!win.shouldClose()) {
			boolean can_render = false;

			double time_2 = Timer.getTime();
			double passed = time_2 - time;
			unprocessed+=passed;
			frame_time += passed;

			time = time_2;

			while(unprocessed >= frame_cap) {
				unprocessed-=frame_cap;
				can_render = true;
				if(win.getInput().isKeyPressed(GLFW_KEY_ESCAPE))
					glfwSetWindowShouldClose(win.getWindow(), true);
				player.update((float)frame_cap, win, camera, world);
				world.correctCamera(camera, win);
				win.update();
				if (frame_time >= 1.0) {
					frame_time = 0;
					System.out.println("FPS: " + frames);
					frames = 0;
				}
			}

			if (can_render) {
				//Clear the buffer
				glClear(GL_COLOR_BUFFER_BIT);

				world.render(tiles, shader, camera, win);

				player.render(shader, camera);

				//Swap buffer at GPU
				win.swapBuffers();
				frames++;
			}

		}
	}

	public static void main(String[] args) {
		new Game().run();
	}

}
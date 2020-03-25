package me.peter.irio.engine;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import java.nio.*;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public class HelloWorld {

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
		win.setSize(3840, 2160);
		win.setFullscreen(true);
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

		Texture tex = new Texture("./environment/textures/Luigi.png");

		Matrix4f scale = new Matrix4f().scale(64);

		Matrix4f target = new Matrix4f();


		float[] verticies = new float[] {
				-0.5f, 0.5f, 0,
				0.5f,0.5F,0,
				0.5f,-0.5f,0,
				-0.5f, -0.5f, 0
		};

		float[] texture = new float[] {
				0,0,
				1,0,
				1,1,
				0,1,

		};

		int[] indicies = new int[] {
				0,1,2,
				2,3,0
		};


		Model model = new Model(verticies, texture, indicies);
		Shader shader = new Shader("shader");

		camera.setPosition(new Vector3f(-100, 0, 0));

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
				target = scale;
				if(glfwGetKey(win.getWindow(), GLFW_KEY_ESCAPE) == GL_TRUE)
					glfwSetWindowShouldClose(win.getWindow(), true);
				//Pull new events
				glfwPollEvents();

				if (frame_time >= 1.0) {
					frame_time = 0;
					System.out.println("FPS: " + frames);
					frames = 0;
				}
			}

			if (can_render) {
				//Clear the buffer
				glClear(GL_COLOR_BUFFER_BIT);

				shader.bind();
				shader.setUniform("sampler", 0);
				shader.setUniform("projection", camera.getProjection().mul(target));
				tex.bind(0);
				model.render();

				//Swap buffer at GPU
				win.swapBuffers();
				frames++;
			}

		}
	}

	public static void main(String[] args) {
		new HelloWorld().run();
	}

}
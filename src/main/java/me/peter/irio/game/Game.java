package me.peter.irio.game;

import me.peter.irio.entity.Player;
import me.peter.irio.io.Timer;
import me.peter.irio.io.Window;
import me.peter.irio.render.*;
import me.peter.irio.world.Tile;
import me.peter.irio.world.TileRenderer;
import me.peter.irio.world.World;
import org.lwjgl.*;
import org.lwjgl.opengl.*;

import java.awt.*;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

/**
 * Klass som sköter skapandet av ett nytt spel.
 */
public class Game {
	//Fönstret som spelet associeras med
	private Window win;

	//Påbörja ett nytt spel
	public void run() {
		System.out.println("Starting LWJGL " + Version.getVersion() + "!");

		//Skapar fönster
		init();
		//Startar renderingen
		startRender();

		//När renderingen är klar så gör vi oss av med fönstret
		win.destroyWindow();

		//Vi avslutar proccessen för renderingen och slutar lyssna efter fel
		glfwTerminate();
		glfwSetErrorCallback(null).free();
	}

	private void init() {
		//Vi börjar lyssna efter fel
		Window.setCallbacks();

		//Initiera GLFW
		if ( !glfwInit() )
			throw new IllegalStateException("Kunde inte starta GLFW");

		//Vi skapar ett nytt fönster
		win = new Window();
		//Sätter storleken till 640x380 pixlar
		win.setSize(640, 380);
		//Visar sedan fönstret på skrivbordet med titeln "Game"
		win.createWindow("Game");

		//Vi gör så att fönstret blir synligt och förbjuder användaren att ändra storleken
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
		glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE); // the window will be resizable

	}

	private void startRender() {
		//Vi kallar på OpenGL och initierar en process där vi kan börja rendera
		GL.createCapabilities();

		//Vår kamera, alltså spelarens synvinkel
		Camera camera = new Camera(win.getWidth(), win.getHeight());

		//Vi vill rendera tvådimensionella texturer
		glEnable(GL_TEXTURE_2D);
		//Vi vill rita på våran yta utan att skapa några nya ytor.
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

		//Kommer att rendera världen
		TileRenderer tiles = new TileRenderer();

		//Själva världen som innehåller det statiska som ska renderas
		World world = new World();

		//En ny spelare
		Player player = new Player();

		//Varje gång buffern byter bild så vill vi förbereda renderingen med en variant av blå till bakgrund
		glClearColor(4, 156, 216, 0);

		//TODO: Flytta allt som har med att skapa världen till en level klass??
		//Sätter alla tiles mellan Y 10-12 och X 0-64 till ground textur. Detta är då marken
		for (int i = 10; i < 12; i++) {
			for (int j = 0; j < 64; j++) {
				world.setTile(Tile.GROUND, j, i);
			}
		}
		//Mer med att sätta olika tiles på världen.
		world.setTile(Tile.QUESTION, 6, 6);
		world.setTile(Tile.BRICKS, 10, 6);
		world.setTile(Tile.QUESTION, 11, 6);
		world.setTile(Tile.BRICKS, 12, 6);
		world.setTile(Tile.QUESTION, 12, 2);
		world.setTile(Tile.QUESTION, 13, 6);
		world.setTile(Tile.BRICKS, 14, 6);

		//Vi skapar en ny shader utifrån shaderfilerna vi har i våran exekviveringsmiljö.
		Shader shader = new Shader("shader");

		//Vi låser renderingen till 60 bilder per sekund.
		double frame_cap = 1.0/60.0;

		//Räknare som räknar till en sekund.
		double frame_time = 0;
		//Räknare som räknar bilder under en sekund.
		int frames= 0;

		//TODO: Försöka rendera text på bilderna
		Text FPSCounter = new Text("Comic Sans MS", Font.BOLD, 16, 10, 10, "FPS: 0");

		//Hämtar systemets tid i nanosekunder
		double time = Timer.getTime();
		double unprocessed = 0;
		while (!win.shouldClose()) {
			boolean can_render = false;

			//Hämtar systemets tid i nanosekunder efter renderingen
			double time_2 = Timer.getTime();
			//Tiden det tog att rendera
			double passed = time_2 - time;
			//Frames som inte ska renderas eftersom att vi inte nått våran framecap än
			unprocessed+=passed;
			frame_time += passed;

			//Tid för nästa bild
			time = time_2;

			//Om tiden för uteblivna frames har nått framecap så hämtar vi indata och börjar tillåter rendering.
			while(unprocessed >= frame_cap) {
				System.out.println("--------------------------------------------");
				unprocessed-=frame_cap;
				can_render = true;
				//Kollar om användern har tryck på ESC för att stänga ner programmet
				if(win.getInput().isKeyPressed(GLFW_KEY_ESCAPE))
					glfwSetWindowShouldClose(win.getWindow(), true);
				//Uppdatera position etc. för spelare
				player.update((float)frame_cap, win, camera, world);
				//Korrigera kameran efter spelaren.
				world.correctCamera(camera, win);
				//Hämta ny indata, ska detta vara först kanske?
				win.update();
				//Om vi har uppnått en sekund sedan senaste utskriften vill vi skriva ut hur många bilder vi
				//renderat på en sekund och återställa räknaren.
				if (frame_time >= 1.0) {
					frame_time = 0;
					System.out.println("FPS: " + frames);
					FPSCounter.updateContent("FPS: " + frames);
					frames = 0;
				}
			}
			//Om vi tillåter rendering kan vi börja rendera.
			if (can_render) {
				//Skriv över bilden vi har i buffern med en statisk färg.
				glClear(GL_COLOR_BUFFER_BIT);

				//Rendera världen
				world.render(tiles, shader, camera, win);

				//Rendera spelaren
				player.render(shader, camera);

				//Rendera texten som ska skriva ut bilder per sekund.
				FPSCounter.render();
				//Byt bilder hos GPU:n.
				win.swapBuffers();
				//Lägg till en renderad bild i räknaren.
				frames++;
			}

		}
	}

	//TODO Ta bort?
	//Tillfällig startpunkt för programmet.
	public static void main(String[] args) {
		new Game().run();
	}

}
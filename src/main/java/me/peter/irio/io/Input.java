package me.peter.irio.io;

import static org.lwjgl.glfw.GLFW.*;

public class Input {
    private long window;

    private boolean keys[];

    public Input(long window) {
        this.window = window;
        this.keys = new boolean[GLFW_KEY_LAST];
        for (int i = 0; i < GLFW_KEY_LAST; i++)
            keys[i] = false;
    }

    public boolean isKeyDown(int key) {
        try {
            return glfwGetKey(window, key) == 1;
        } catch(IllegalStateException e) {
            return false;
        }
    }

    public boolean isKeyPressed(int key) {
        return (isKeyDown(key) && !keys[key]);
    }

    public boolean isKeyReleased(int key) {
        return (!isKeyDown(key) && keys[key]);
    }

    public boolean isMouseButtonDown(int key) {
        return glfwGetMouseButton(window, key) == 1;
    }

    public void update() {
        for (int i = 0; i < GLFW_KEY_LAST; i++)
            keys[i] = isKeyDown(i);
    }

}

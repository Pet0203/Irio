package me.peter.irio.world;

import me.peter.irio.render.Camera;
import me.peter.irio.render.Model;
import me.peter.irio.render.Shader;
import me.peter.irio.render.Texture;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.HashMap;

public class TileRenderer {
    private HashMap<String, Texture> tile_textures;
    private Model model;

    public TileRenderer() {
        tile_textures = new HashMap<>();
        float[] verticies = new float[] {
                -1f, 1f, 0,
                1f,1f,0,
                1f,-1f,0,
                -1f, -1f, 0
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

        model = new Model(verticies, texture, indicies);

        for (int i = 0; i < Tile.tiles.length; i++) {
            if (Tile.tiles[i] != null) {
                if (!tile_textures.containsKey(Tile.tiles[i].getTexture())) {
                    String tex = Tile.tiles[i].getTexture();
                    tile_textures.put(tex, new Texture(tex + ".png"));
                }
            }
        }
    }

    public void renderTile(Tile tile, int x, int y, Shader shader, Matrix4f world, Camera camera) {
        shader.bind();
        if (tile_textures.containsKey(tile.getTexture()))
            tile_textures.get(tile.getTexture()).bind(0);

        Matrix4f tile_pos = new Matrix4f().translate(new Vector3f(x*2, y*2, 0));
        Matrix4f target = new Matrix4f();

        camera.getProjection().mul(world, target);
        target.mul(tile_pos);

        shader.setUniform("sampler", 0);
        shader.setUniform("projection", target);

        model.render();
    }
}

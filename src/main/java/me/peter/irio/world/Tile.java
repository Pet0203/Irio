package me.peter.irio.world;

public class Tile {
    public static Tile tiles[] = new Tile[16];
    public static byte not = 0;

    public static final Tile AIR = new Tile((byte)0, "air");
    public static final Tile GROUND = new Tile((byte)1, "ground_block").setSolid();
    public static final Tile QUESTION = new Tile((byte)2, "question").setSolid();
    public static final Tile BRICKS = new Tile((byte)3, "bricks").setSolid();

    private byte id;
    private boolean solid;
    private String texture;

    public Tile(byte id, String texture) {
        this.id = not;
        not++;
        this.texture = texture;
        if(tiles[id] != null)
            throw new IllegalStateException("Tiles at: ["+id+"] is already being used!");
        tiles[id] = this;
        this.solid = false;
    }

    public byte getId() { return id; }

    public String getTexture() { return texture; }

    public Tile setSolid() { this.solid = true;  return this;}

    public boolean isSolid() { return solid; }
}

package me.peter.irio.render;

import me.peter.irio.render.font.TrueTypeFont;

import java.awt.*;

public class Text {

    private TrueTypeFont font;
    private Font awtFont;
    private float posX;
    private float posY;
    private String content;

    public Text(String fontName, int type, int size, float posX, float posY, String content) {
        this.posX = posX;
        this.posY = posY;
        this.content = content;
        awtFont = new Font(fontName, type, size);
        font = new TrueTypeFont(awtFont, true);
    }

    public void render() {
        font.drawString(posX, posY, content, 1, 1);
    }

    public void updateContent(String content) {
        this.content = content;
    }
}

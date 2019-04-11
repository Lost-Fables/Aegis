package net.lordofthecraft.aegis.bukkit;

import lombok.Getter;
import net.lordofthecraft.aegis.bukkit.MapFont.CharacterSprite;

public class MapCanvas {
    @Getter private final byte[] buffer = new byte[128 * 128];

    public void setPixel(int x, int y, byte color) {
        if (x < 0 || y < 0 || x >= 128 || y >= 128)
            return;
        if (buffer[y * 128 + x] != color) {
            buffer[y * 128 + x] = color;
        }
    }

    public void drawText(int x, int y, String text) {
        int xStart = x;
        byte color = 116;
        MapFont font = MinecraftFont.Font;
        if (!font.isValid(text)) {
            throw new IllegalArgumentException("text contains invalid characters");
        }

        for (int i = 0; i < text.length(); ++i) {
            char ch = text.charAt(i);
            if (ch == '\n') {
                x = xStart;
                y += font.getHeight() + 1;
                continue;
            } else if (ch == '\u00A7') {
                int j = text.indexOf(';', i);
                if (j >= 0) {
                    try {
                        color = Byte.parseByte(text.substring(i + 1, j));
                        i = j;
                        continue;
                    }
                    catch (NumberFormatException ex) {}
                }
                throw new IllegalArgumentException("Text contains unterminated color string");
            }

            CharacterSprite sprite = font.getChar(text.charAt(i));
            for (int r = 0; r < font.getHeight(); ++r) {
                for (int c = 0; c < sprite.getWidth(); ++c) {
                    if (sprite.get(r, c)) {
                        setPixel(x + c, y + r, color);
                    }
                }
            }
            x += sprite.getWidth() + 1;
        }
    }

}

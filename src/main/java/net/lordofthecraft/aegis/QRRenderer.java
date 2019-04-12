package net.lordofthecraft.aegis;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import lombok.SneakyThrows;
import net.lordofthecraft.aegis.bukkit.MapCanvas;

public class QRRenderer{
    private static final String ENCODE_FORMAT = "otpauth://totp/%s@%s?secret=%s";
    private static final byte BLACK = (byte) 116;
    private static final byte WHITE = (byte) 32;

    private BitMatrix bitMatrix = null;
    private final String secret;
    
    public QRRenderer(String username, String secret, String topbar) {
        bitMatrix = getQRMap(username, secret, topbar);
        this.secret = secret;
    }
    

    public byte[] render() {
        MapCanvas c = new MapCanvas();
        for (int x = 0; x < 128; x++) {
            for (int y = 0; y < 128; y++) {
                boolean black = bitMatrix.get(x, y);
                if(black) c.setPixel(x, y, BLACK);
                else c.setPixel(x, y, WHITE);
            }
        }
        
    		c.drawText(5, 5, "Key: " + secret);
    		c.drawText(5, 120, "/auth ######");
        return c.getBuffer();
    }

    @SneakyThrows
    private BitMatrix getQRMap(String username, String secret, String topBar) {
        return new QRCodeWriter().encode(String.format(ENCODE_FORMAT,
                username,
                topBar,
                secret),
                BarcodeFormat.QR_CODE, 128, 128);
    }

}

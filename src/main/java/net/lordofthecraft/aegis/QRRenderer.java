package net.lordofthecraft.aegis;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import lombok.SneakyThrows;

public class QRRenderer{
    private static final String ENCODE_FORMAT = "otpauth://totp/%s@%s?secret=%s";
    private static final byte BLACK = (byte) 29;
    private static final byte WHITE = (byte) 8;

    private BitMatrix bitMatrix = null;

    public QRRenderer(String username, String secret, String topbar) {
        bitMatrix = getQRMap(username, secret, topbar);
    }
    

    public byte[] render() {
        byte[] bytes = new byte[128*128];
        for (int x = 0; x < 128; x++) {
            for (int y = 0; y < 128; y++) {
                int index = x*128 + y;
                boolean black = bitMatrix.get(x, y);
                if(black) bytes[index] = BLACK;
                else bytes[index] = WHITE;
            }
        }
        
        return bytes;
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

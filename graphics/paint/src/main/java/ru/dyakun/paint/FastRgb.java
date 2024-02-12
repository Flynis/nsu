package ru.dyakun.paint;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

public class FastRgb {

    private final int width;
    private final int height;
    private final boolean hasAlphaChannel;
    private final int pixelLength;
    private final byte[] pixels;

    public FastRgb(BufferedImage image) {
        pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        width = image.getWidth();
        height = image.getHeight();
        hasAlphaChannel = image.getAlphaRaster() != null;
        pixelLength = (hasAlphaChannel) ? 4 : 3;
    }

    int getRGB(int x, int y) {
        int pos = (y * pixelLength * width) + (x * pixelLength);
        int argb = -16777216; // 255 alpha
        if (hasAlphaChannel) {
            argb = (((int) pixels[pos++] & 0xff) << 24); // alpha
        }
        argb += ((int) pixels[pos++] & 0xff); // blue
        argb += (((int) pixels[pos++] & 0xff) << 8); // green
        argb += (((int) pixels[pos] & 0xff) << 16); // red
        return argb;
    }
}

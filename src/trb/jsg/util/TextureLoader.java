package trb.jsg.util;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import org.lwjgl.BufferUtils;

public class TextureLoader {

    public static ByteBuffer getImageData(BufferedImage image) {
        int w = image.getWidth();
        int h = image.getHeight();
        ByteBuffer byteBuffer = BufferUtils.createByteBuffer(w * h * 4);
        byteBuffer.asIntBuffer().put(image.getRGB(0, 0, w, h, null, 0, w)).rewind();
        return byteBuffer;
    }
}

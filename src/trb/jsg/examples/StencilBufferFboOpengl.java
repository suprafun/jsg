package trb.jsg.examples;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import org.lwjgl.BufferUtils;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.PixelFormat;
import trb.jsg.View;
import trb.jsg.util.GLUtils;

public class StencilBufferFboOpengl {
    public static void main(String[] args) throws Exception {
        Display.setDisplayMode(new DisplayMode(640, 480));
        Display.create(new PixelFormat().withStencilBits(8));

        ByteBuffer pixels = BufferUtils.createByteBuffer(256 * 256 * 4);
        for (int i=0; i<pixels.limit(); i++) {
            pixels.put(i, (byte) (Math.random() * 0xff));
        }
        IntBuffer textureId = BufferUtils.createIntBuffer(1);
        glGenTextures(textureId);
        glBindTexture(GL_TEXTURE_2D, textureId.get(0));
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, 256, 256, 0, GL_RGBA, GL_UNSIGNED_BYTE, pixels);

        IntBuffer fboId = BufferUtils.createIntBuffer(1);
        glGenFramebuffers(fboId);
        glBindFramebuffer(GL_FRAMEBUFFER, fboId.get(0));

        IntBuffer depthId = BufferUtils.createIntBuffer(1);
        glGenRenderbuffers(depthId);
        glBindRenderbuffer(GL_RENDERBUFFER, depthId.get(0));
        glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT, 256, 256);
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, depthId.get(0));
        //glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_STENCIL_ATTACHMENT, GL_RENDERBUFFER, depthId.get(0));
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, textureId.get(0), 0);

        if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
            Thread.dumpStack();
        }


        IntBuffer drawBuffers = BufferUtils.createIntBuffer(1);
        drawBuffers.put(0, GL_COLOR_ATTACHMENT0);
        glBindFramebuffer(GL_FRAMEBUFFER, fboId.get(0));
        glDrawBuffers(drawBuffers);
        glViewport(0, 0, 256, 256);

        glClearColor(0, 1, 1, 1);
        glClearStencil(1);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);

        glMatrixMode(GL_PROJECTION);
        GLUtils.loadMatrix(View.createOrtho(0, 640, 480, 0, -1000, 1000).getProjectionMatrix());
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();

        glEnable(GL_STENCIL_TEST);
        glStencilFunc(GL_EQUAL, 0, 1);
        glBegin(GL_TRIANGLES);
        glColor3f(1, 0, 0);
        glVertex2f(100, 100);
        glVertex2f(200, 100);
        glVertex2f(200, 200);
        glVertex2f(200, 200);
        glVertex2f(100, 200);
        glVertex2f(100, 100);
        glEnd();
        glStencilFunc(GL_EQUAL, 1, 1);
        glBegin(GL_TRIANGLES);
        glColor3f(0, 0, 1);
        glVertex2f(300, 100);
        glVertex2f(400, 100);
        glVertex2f(400, 200);
        glVertex2f(400, 200);
        glVertex2f(300, 200);
        glVertex2f(300, 100);
        glEnd();
        glDisable(GL_STENCIL_TEST);

        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glDrawBuffer(GL_BACK);

        glMatrixMode(GL_PROJECTION);
        GLUtils.loadMatrix(View.createOrtho(0, 640, 480, 0, -1000, 1000).getProjectionMatrix());
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();


        while (!Display.isCloseRequested()) {

            glClearColor(0, 0.3f, 0, 1);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);

            glEnable(GL_TEXTURE_2D);
            glBindTexture(GL_TEXTURE_2D, textureId.get(0));

            glBegin(GL_TRIANGLES);
            glColor3f(1, 1, 1);
            glTexCoord2f(0, 0); glVertex2f(100, 0);
            glTexCoord2f(1, 0); glVertex2f(320, 0);
            glTexCoord2f(1, 1); glVertex2f(320, 240);

            glTexCoord2f(1, 1); glVertex2f(320, 240);
            glTexCoord2f(0, 1); glVertex2f(100, 240);
            glTexCoord2f(0, 0); glVertex2f(100, 0);
            glEnd();

            Display.update();
        }

        Display.destroy();
    }
}

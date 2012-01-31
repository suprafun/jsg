package trb.jsg.examples;

import static org.lwjgl.opengl.GL11.*;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.PixelFormat;
import trb.jsg.View;
import trb.jsg.util.GLUtils;

public class StencilBufferOpengl {
    public static void main(String[] args) throws Exception {
        Display.setDisplayMode(new DisplayMode(640, 480));
        Display.create(new PixelFormat().withStencilBits(8));

        glMatrixMode(GL_PROJECTION);
        GLUtils.loadMatrix(View.createOrtho(0, 640, 0, 480, -1000, 1000).getProjectionMatrix());

        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();

        while (!Display.isCloseRequested()) {
            glEnable(GL_STENCIL_TEST);

            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);

            glStencilFunc(GL_ALWAYS, 1, 1);
            glStencilOp(GL_REPLACE, GL_REPLACE, GL_REPLACE);
            glBegin(GL_TRIANGLES);
            glColor3f(1, 0, 0);
            glVertex2f(100, 100);
            glVertex2f(200, 100);
            glVertex2f(200, 200);
            glVertex2f(200, 200);
            glVertex2f(100, 200);
            glVertex2f(100, 100);
            glEnd();


            glStencilFunc(GL_EQUAL, 0, 1);
            glStencilOp(GL_KEEP, GL_KEEP, GL_KEEP);
            glBegin(GL_TRIANGLES);
            glColor3f(0, 1, 0);
            glVertex2f(0, 0);
            glVertex2f(400, 0);
            glVertex2f(400, 400);
            glVertex2f(400, 400);
            glVertex2f(0, 400);
            glVertex2f(0, 0);
            glEnd();

            Display.update();
        }

        Display.destroy();
    }
}

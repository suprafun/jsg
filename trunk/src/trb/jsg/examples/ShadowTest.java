package trb.jsg.examples;

import java.nio.ByteBuffer;
import javax.vecmath.Color4f;
import org.lwjgl.BufferUtils;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import trb.jsg.DepthBuffer;

import trb.jsg.RenderPass;
import trb.jsg.RenderTarget;
import trb.jsg.SceneGraph;
import trb.jsg.Shader;
import trb.jsg.ShaderProgram;
import trb.jsg.Shape;
import trb.jsg.Texture;
import trb.jsg.Uniform;
import trb.jsg.Unit;
import trb.jsg.View;
import trb.jsg.enums.Format;
import trb.jsg.enums.TextureType;
import trb.jsg.enums.Wrap;
import trb.jsg.renderer.Renderer;
import trb.jsg.util.Mat4;
import trb.jsg.util.SGUtil;
import trb.jsg.util.Vec3;
import trb.jsg.util.geometry.VertexDataUtils;

public class ShadowTest {

    private static String vertexShader =
            "varying vec3 posv;\n"
            + "void main(void) {\n"
            + "    posv = ( gl_ModelViewMatrix * gl_Vertex ).xyz;\n"
            + "    gl_Position = ftransform();\n"
            + "}";

    private static String fragmentShader = ""
            + "varying vec3 posv;\n"
            + "uniform mat4 viewToLight;\n"
            + "uniform sampler2D texture;\n"
            + "uniform vec3 lightPosVS;\n"
            + "void main( void ){\n"
            + "    float distanceToLight = length(posv - lightPosVS);\n"
            + "    vec4 posl = viewToLight * vec4(posv, 1);\n"
            + "    posl /= posl.w;\n"
            + "    float distanceToShadowCaster = texture2D(texture, posl.xy).x;\n"
            + "    gl_FragColor = abs(distanceToLight - distanceToShadowCaster);\n"

//            + "    vec4 color = vec4(1.0);\n"
//            + "    if (distanceToShadowCaster+0.4 < distanceToLight) {;\n"
//            + "       color = vec4(0.0);\n"
//            + "    };\n"
//            + "    gl_FragColor = color;\n"
            + "}\n";

    private static String spotlightFragmentShader = ""
            + "varying vec3 posv;\n"
            + "void main( void ){\n"
            + "    gl_FragColor = length(posv);\n"
            + "}\n";

	public static void main(String[] args) throws Exception {
		Display.setDisplayMode(new DisplayMode(640, 480));
		Display.create();

		Shape baseBox = new Shape(VertexDataUtils.createBox(new Vec3(-1, 1, -1), new Vec3(1, 3, 1)));
		Texture baseTexture = SGUtil.createTexture(GL11.GL_RGBA, 128, 128);
		RenderTarget baseTarget = new RenderTarget(128, 128, new DepthBuffer(GL30.GL_DEPTH24_STENCIL8), false, baseTexture);
		RenderPass basePass = new RenderPass();
		basePass.setClearMask(GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_COLOR_BUFFER_BIT);
		basePass.setClearColor(new Color4f(0, 1, 1, 0));
		basePass.setView(View.createPerspective((float)Math.PI / 4f, 1, 0.1f, 100f));
		basePass.getView().setCameraMatrix(new Mat4().rotateEulerDeg(-30, 0, 0).translate(0, 0, 10).invert_());
		basePass.setRenderTarget(baseTarget);
		basePass.addShape(new Shape(VertexDataUtils.createBox(new Vec3(-4, -1, -4), new Vec3(4, 0, 4))));
		basePass.addShape(baseBox);

		Texture shadowTexture = SGUtil.createTexture(GL30.GL_R32F, 128, 128);
                shadowTexture.setWrapS(Wrap.CLAMP);
                shadowTexture.setWrapT(Wrap.CLAMP);
		RenderTarget shadowTarget = new RenderTarget(128, 128, new DepthBuffer(GL11.GL_DEPTH_COMPONENT), false, shadowTexture);
		RenderPass shadowPass = new RenderPass();
		shadowPass.setClearMask(GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_COLOR_BUFFER_BIT);
		shadowPass.setClearColor(new Color4f(0, 0, 1, 0));
		shadowPass.setView(View.createPerspective((float) Math.PI / 4f, 1, 0.1f, 100f));
		shadowPass.setRenderTarget(shadowTarget);
		shadowPass.addShape(new Shape(VertexDataUtils.createBox(new Vec3(-4, -1, -4), new Vec3(4, 0, 4))));
		shadowPass.addShape(new Shape(VertexDataUtils.createBox(new Vec3(-1, 1, -1), new Vec3(1, 3, 1))));
		shadowPass.getView().setCameraMatrix(new Mat4().rotateEulerDeg(-90-45, 0, 0).translate(0, 0, 10).invert_());
		//Renderer shadowRenderer = new Renderer(new SceneGraph(shadowPass));

		Shape baseShape = new Shape(VertexDataUtils.createQuad(50, 100+256, 256, -256, 0));
		baseShape.getState().setUnit(0, new Unit(baseTexture));
		Shape shadowShape = new Shape(VertexDataUtils.createQuad(350, 100, 256, 256, 0));
		shadowShape.getState().setUnit(0, new Unit(shadowTexture));
		RenderPass finalPass = new RenderPass();
		finalPass.setClearMask(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		finalPass.setClearColor(new Color4f(1, 1, 0, 0));
		finalPass.setView(View.createOrtho(0, 640, 0, 480, -1000, 1000));
		finalPass.addShape(baseShape);
		finalPass.addShape(shadowShape);

                //Texture testTexture = createSampleTexture();
                shadowStuff(basePass, shadowPass, shadowTexture);

		SceneGraph finalSceneGraph = new SceneGraph();
		finalSceneGraph.addRenderPass(shadowPass);
		finalSceneGraph.addRenderPass(basePass);
		finalSceneGraph.addRenderPass(finalPass);
		Renderer finalRenderer = new Renderer(finalSceneGraph);

		long startTime = System.currentTimeMillis();
		while (!Display.isCloseRequested()) {
			float timeSec = (System.currentTimeMillis() - startTime) / 1000f;
			baseBox.setModelMatrix(new Mat4().rotateEulerDeg(0, timeSec * 45, 0));

                        for (int i=0; i<basePass.getShapeCount(); i++) {
                            shadowPass.getShape(i).setModelMatrix(basePass.getShape(i).getModelMatrix());
                        }

			//shadowRenderer.render();
			finalRenderer.render();
			Display.update();
		}

		Display.destroy();
	}

        static void shadowStuff(RenderPass basePass, RenderPass lightPass, Texture shadowTexture) {
            Mat4 viewTransform = basePass.getView().getCameraMatrix();
            Mat4 lightTransform = lightPass.getView().getCameraMatrix();

            Mat4 homogenToTexCoord = new Mat4().translate(0.5f, 0.5f, 0f).scale(0.5, 0.5, 0.5);

            Mat4 viewToTexture = new Mat4();
            viewToTexture.mul(homogenToTexCoord);
            viewToTexture.mul(lightPass.getView().getProjectionMatrix());
            viewToTexture.mul_(new Mat4(lightTransform));
            viewToTexture.mul_(new Mat4(viewTransform).invert_());

            Mat4 lightToView = new Mat4();
            lightToView.mul_(new Mat4(viewTransform));
            lightToView.mul_(new Mat4(lightTransform).invert_());
            Vec3 lightPosVS = lightToView.transformAsPoint(new Vec3());


        //Vec3 vs = new Mat4(view.getCameraMatrix()).transformAsPoint(new Vec3(positionWorld));

            Shader shader = createShader(viewToTexture);
            shader.putUniform(new Uniform("lightPosVS", Uniform.Type.VEC3, lightPosVS.toFloats()));
            for (Shape shape : basePass.getAllShapes()) {
                shape.getState().setShader(shader);
                shape.getState().setUnit(0, new Unit(shadowTexture));
            }

            Shader spotLightShader = new Shader(new ShaderProgram(vertexShader, spotlightFragmentShader));
            for (Shape shape : lightPass.getAllShapes()) {
                shape.getState().setShader(spotLightShader);
            }
        }

        static Shader createShader(Mat4 viewToLight) {
            Shader shader = new Shader(new ShaderProgram(vertexShader, fragmentShader));
            shader.putUniform(new Uniform("viewToLight", Uniform.Type.MAT4, getTransposedFloats(viewToLight)));
            return shader;
        }

        static float[] getTransposedFloats(Mat4 transform){
            Mat4 m = new Mat4(transform);
            m.transpose();
            return m.toFloats();
        }

        static Texture createSampleTexture() {
            ByteBuffer byteBuffer = BufferUtils.createByteBuffer(256 * 256 * 4);
            for (int y = 0; y < 256; y++) {
                for (int x = 0; x < 256; x++) {
                    byte b = (byte) (x ^ y);
                    byteBuffer.put(b).put(b).put(b).put(b);
                }
            }
            ByteBuffer[][] pixels = {{byteBuffer}};
            return new Texture(TextureType.TEXTURE_2D, 4, 256, 256, 0, Format.BGRA, pixels, false, false);
        }
}

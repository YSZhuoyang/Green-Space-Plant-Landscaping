package Support;

import static Support.Resources.displayHeight;
import static Support.Resources.displayWidth;
import static Support.Resources.gl;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import com.jogamp.opengl.GL4;
import com.jogamp.opengl.fixedfunc.GLMatrixFunc;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.PMVMatrix;

/**
 * @author Yu Sangzhuoyang
 * @version 5.05
 */
public class Camera
{
	private static int mvTransUniBuffIndex;
	private static int billUniBuffIndex;
	
	private static int mvTransUniBuffLen;
	private static int billUniBuffLen;
	private static int pixelBuffLen;
	
	private static int uboCount;
	private static int pboCount;
	
	private static float[] viewMat;
	private static float[] proMat;
	
	private static float moveSpeed = 0.05f;				//Moving speed of camera.
	
	private static float[] topPos = {0f, 50f, 0f};
	private static float[] topView = {0f, -1f, 0f};
	private static float[] topUp = {1f, 0f, 0f};
	
	private static float[] freePos = {0f, 10f, 20f};
	private static float[] freeView = {0f, 0f, -1f};
	private static float[] freeUp = {0f, 1f, 0f};
	
	private static float[] yAxis = {0, 1, 0};
	
	private static float[] position;
	private static float[] view;
	private static float[] right;
	private static float[] target;
	private static float[] up;
	
	private static float horizontalAngle;
	private static float verticalAngle;
	
	private static PMVMatrix pmvMat;
	
	private static int screenCoordX;
	private static int screenCoordY;
	
	private static int[] startCoord;
	private static int[] endCoord;
	
	private static IntBuffer uboBuff;
	private static IntBuffer pboBuff;
	//private static FloatBuffer zBuff;
	private static ByteBuffer pixelDepthBuff;
	private static FloatBuffer mvTransUniBuff;
	private static FloatBuffer billUniBuff;
	
	public Camera() {}
	
	public static void init()
	{
		viewMat = new float[16];
		proMat = new float[16];
		
		mvTransUniBuffIndex = 7;
		billUniBuffIndex = 8;
		
		mvTransUniBuffLen = 16 * Float.SIZE / 8 * 2;
		billUniBuffLen = 4 * Float.SIZE / 8 * 2;
		pixelBuffLen = Float.SIZE / 8;
		
		uboCount = 2;
		pboCount = 1;
		
		horizontalAngle = 3.14f;
		verticalAngle = 0f;
		
		position = new float[3];
		view = new float[3];
		right = new float[3];
		up = new float[3];
		target = new float[3];
		
		startCoord = new int[2];
		endCoord = new int[2];
		
		//zBuff = GLBuffers.newDirectFloatBuffer(1);
		uboBuff = GLBuffers.newDirectIntBuffer(uboCount);
		pboBuff = GLBuffers.newDirectIntBuffer(pboCount);
		mvTransUniBuff = GLBuffers.newDirectFloatBuffer(mvTransUniBuffLen);
		billUniBuff = GLBuffers.newDirectFloatBuffer(billUniBuffLen);
		
		pmvMat = new PMVMatrix();
		
		setPosition(freePos[0], freePos[1], freePos[2]);
		setDirection(freeView[0], freeView[1], freeView[2]);
		setUp(freeUp[0], freeUp[1], freeUp[2]);
		
		setupBuffers();
	}
	
	public static void look()
	{
		//notice that the model matrix should be stored in the model objects, 
		//for picking and moving themselves by users
		pmvMat.glLoadIdentity();
		pmvMat.gluLookAt(position[0], position[1], position[2], target[0], target[1], target[2], up[0], up[1], up[2]);
		
		pmvMat.glGetFloatv(GLMatrixFunc.GL_MODELVIEW, viewMat, 0);
		pmvMat.glGetFloatv(GLMatrixFunc.GL_PROJECTION, proMat, 0);
		
		updateBuffers();
	}
	
	public static void setupBuffers()
	{
		gl.glGenBuffers(uboCount, uboBuff);
		
		gl.glBindBuffer(GL4.GL_UNIFORM_BUFFER, uboBuff.get(0));
		gl.glBufferData(GL4.GL_UNIFORM_BUFFER, mvTransUniBuffLen, null, GL4.GL_DYNAMIC_DRAW);
		
		gl.glBindBuffer(GL4.GL_UNIFORM_BUFFER, uboBuff.get(1));
		gl.glBufferData(GL4.GL_UNIFORM_BUFFER, billUniBuffLen, null, GL4.GL_DYNAMIC_DRAW);
		
		gl.glBindBufferBase(GL4.GL_UNIFORM_BUFFER, mvTransUniBuffIndex, uboBuff.get(0));
		gl.glBindBufferBase(GL4.GL_UNIFORM_BUFFER, billUniBuffIndex, uboBuff.get(1));
		
		gl.glGenBuffers(pboCount, pboBuff);
		gl.glBindBuffer(GL4.GL_PIXEL_PACK_BUFFER, pboBuff.get(0));
		gl.glBufferData(GL4.GL_PIXEL_PACK_BUFFER, pixelBuffLen, null, GL4.GL_DYNAMIC_READ);
	}
	
	public static void updateBuffers()
	{
		mvTransUniBuff.clear();
		billUniBuff.clear();
		
		mvTransUniBuff.put(viewMat);
		mvTransUniBuff.put(proMat);
		mvTransUniBuff.rewind();
		
		billUniBuff.put(yAxis);
		billUniBuff.put(0);			//padding
		billUniBuff.put(position);
		billUniBuff.put(0);			//padding
		billUniBuff.rewind();
		
		gl.glBindBuffer(GL4.GL_UNIFORM_BUFFER, uboBuff.get(0));
		gl.glBufferSubData(GL4.GL_UNIFORM_BUFFER, 0, mvTransUniBuffLen, mvTransUniBuff);
		
		gl.glBindBuffer(GL4.GL_UNIFORM_BUFFER, uboBuff.get(1));
		gl.glBufferSubData(GL4.GL_UNIFORM_BUFFER, 0, billUniBuffLen, billUniBuff);
	}
	
	public static void resetTopView()
	{
		setPosition(topPos[0], topPos[1], topPos[2]);
		setDirection(topView[0], topView[1], topView[2]);
		setUp(topUp[0], topUp[1], topUp[2]);
	}
	
	public static void resetFreeLook()
	{
		horizontalAngle = 3.14f;
		verticalAngle = 0f;
		
		setPosition(freePos[0], freePos[1], freePos[2]);
		setDirection(freeView[0], freeView[1], freeView[2]);
		setUp(freeUp[0], freeUp[1], freeUp[2]);
	}
	
	public static void resetMatrices(float aspect)
	{
		pmvMat.glMatrixMode(GLMatrixFunc.GL_PROJECTION);
		pmvMat.glLoadIdentity();
		pmvMat.gluPerspective(45.0f, aspect, 1.0f, 100f);
		
		pmvMat.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
		//pmvMat.glLoadIdentity();
	}
	
	public static void computeTarget()
	{
		target[0] = position[0] + view[0];
		target[1] = position[1] + view[1];
		target[2] = position[2] + view[2];
	}
	
	public static float[] computeGLPos(int x, int y)
	{
		float winX = x;
		float winY = displayHeight - y;
		float winZ;
		
		int[] viewPort = {0, 0, displayWidth, displayHeight};
		float[] glPos = new float[3];
		
		gl.glBindBuffer(GL4.GL_PIXEL_PACK_BUFFER, pboBuff.get(0));
		//gl.glReadPixels((int) winX, (int) winY, 1, 1, GL4.GL_DEPTH_COMPONENT, GL4.GL_FLOAT, zBuff);
		gl.glReadPixels((int) winX, (int) winY, 1, 1, GL4.GL_DEPTH_COMPONENT, GL4.GL_FLOAT, 0);
		
		pixelDepthBuff = gl.glMapBufferRange(GL4.GL_PIXEL_PACK_BUFFER, 0, pixelBuffLen, GL4.GL_MAP_READ_BIT);
		winZ = pixelDepthBuff.getFloat(0);
		gl.glUnmapBuffer(GL4.GL_PIXEL_PACK_BUFFER);
		
		//winZ = zBuff.get(0);
		
		pmvMat.gluUnProject(winX, winY, winZ, viewPort, 0, glPos, 0);
		
		return glPos;
	}
	
	public static void rotate(float angle)
	{
		horizontalAngle += angle;
		
		float sinYaw = (float) Math.sin(horizontalAngle);
		float cosYaw = (float) Math.cos(horizontalAngle);
		float sinPitch = (float) Math.sin(verticalAngle);
		float cosPitch = (float) Math.cos(verticalAngle);
		
		view[0] = cosPitch * sinYaw;
		view[1] = sinPitch;
		view[2] = cosPitch * cosYaw;
		
		up[0] = sinYaw * sinPitch;
		up[1] = cosPitch;
		up[2] = cosYaw * sinPitch;
		
		right[0] = cosYaw;
		right[1] = 0f;
		right[2] = -sinYaw;
		
		computeTarget();
	}
	
	public static void moveForward()
	{
		position[0] += moveSpeed * view[0];
		position[1] += moveSpeed * view[1];
		position[2] += moveSpeed * view[2];
		target[0] += moveSpeed * view[0];
		target[1] += moveSpeed * view[1];
		target[2] += moveSpeed * view[2];
	}
	
	public static void moveBackward()
	{
		position[0] -= moveSpeed * view[0];
		position[1] -= moveSpeed * view[1];
		position[2] -= moveSpeed * view[2];
		target[0] -= moveSpeed * view[0];
		target[1] -= moveSpeed * view[1];
		target[2] -= moveSpeed * view[2];
	}
	
	public static void moveUpward()
	{
		position[1] += moveSpeed;
		target[1] += moveSpeed;
	}
	
	public static void moveDownward()
	{
		position[1] -= moveSpeed;
		target[1] -= moveSpeed;
	}
	
	public static void setPosition(float x, float y, float z)
	{
		position[0] = x;
		position[1] = y;
		position[2] = z;
	}
	
	public static void setTarget(float x, float y, float z)
	{
		target[0] = x;
		target[1] = y;
		target[2] = z;
	}
	
	public static void setDirection(float x, float y, float z)
	{
		view[0] = x;
		view[1] = y;
		view[2] = z;
		
		Vec3f.normalize(view);
		
		computeTarget();
	}
	
	public static void setRight(float x, float y, float z)
	{
		right[0] = x;
		right[1] = y;
		right[2] = z;
		
		Vec3f.normalize(right);
	}
	
	public static void setUp(float x, float y, float z)
	{
		up[0] = x;
		up[1] = y;
		up[2] = z;
		
		Vec3f.normalize(up);
	}
	
	public static void setScreenPos(int x, int y)
	{
		screenCoordX = x;
		screenCoordY = y;
	}
	
	public static void setStartPos(int x, int y)
	{
		startCoord[0]= x;
		startCoord[1]= y;
	}
	
	public static void setEndPos(int x, int y)
	{
		endCoord[0] = x;
		endCoord[1] = y;
	}
	
	public static float[] getPosition()
	{
		return position;
	}
	
	public static float[] getDirection()
	{
		return view;
	}
	
	public static int[] getScreenStartPos()
	{
		return startCoord;
	}
	
	public static int[] getScreenEndPos()
	{
		return endCoord;
	}
	
	public static float[] getClickPos()
	{
		return computeGLPos(screenCoordX, screenCoordY);
	}
	
	public static float[] getPressPos()
	{
		return computeGLPos(startCoord[0], startCoord[1]);
	}
	
	public static float[] getReleasePos()
	{
		return computeGLPos(endCoord[0], endCoord[1]);
	}
}
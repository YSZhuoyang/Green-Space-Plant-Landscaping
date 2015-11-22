package Support;

import static Support.Resources.*;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import com.jogamp.opengl.GL4;
import com.jogamp.opengl.fixedfunc.GLMatrixFunc;
import com.jogamp.opengl.math.Matrix4;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.PMVMatrix;


/**
 * @author Yu Sangzhuoyang
 * @version 5.05
 */
public class Light
{
	private static float[] moveLeft = {-0.5f, 0, 0};		//Left movement.
	private static float[] moveRight = {0.5f, 0, 0};		//Right movement.
	
	private static float[] initPosition = {50f, 100f, 50f};
	private static float[] initTarget = {0f, 0f, 0f};
	
	private static float[] scale_bias_Mat = {0.5f, 0.0f, 0.0f, 0.0f, 
											0.0f, 0.5f, 0.0f, 0.0f, 
											0.0f, 0.0f, 0.5f, 0.0f, 
											0.5f, 0.5f, 0.5f, 1.0f};
	
	private static float[] lightMat;
	private static float[] shadowMat;
	
	private static float[] position;
	private static float[] target;
	
	private static int shadingUniBuffIndex;			//contains light_pos and shadow_mat
	private static int lightMatUniBuffIndex;
	
	private static int shadingUniBuffLen;
	private static int lightMatUniBuffLen;
	
	private static int uboCount;
	
	private static Matrix4 shadow;
	private static PMVMatrix pmvMatrix;
	
	private static IntBuffer uboBuff;
	private static FloatBuffer shadingUniBuff;
	private static FloatBuffer lightMatUniBuff;
	
	public Light() {}
	
	public static void init()
	{
		lightMat = new float[16];
		shadowMat = new float[16];
		
		shadingUniBuffIndex = 9;
		lightMatUniBuffIndex = 10;
		
		shadingUniBuffLen = 16 * Float.SIZE / 8 * 3;
		lightMatUniBuffLen = 16 * Float.SIZE / 8;
		
		uboCount = 2;
		
		position = new float[3];
		target = new float[3];
		
		shadow = new Matrix4();
		
		pmvMatrix = new PMVMatrix();
		pmvMatrix.glMatrixMode(GLMatrixFunc.GL_PROJECTION);
		pmvMatrix.glLoadIdentity();
		pmvMatrix.glFrustumf(-1f, 1f, -1f, 1f, 1f, 200f);
		pmvMatrix.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
		
		uboBuff = GLBuffers.newDirectIntBuffer(uboCount);
		shadingUniBuff = GLBuffers.newDirectFloatBuffer(shadingUniBuffLen);
		lightMatUniBuff = GLBuffers.newDirectFloatBuffer(lightMatUniBuffLen);
		
		setPosition(initPosition);
		setTarget(initTarget);
		
		setupBuffers();
	}
	
	public static void illuminate()
	{
		pmvMatrix.glLoadIdentity();
		pmvMatrix.gluLookAt(position[0], position[1], position[2], target[0], target[1], target[2], 0f, 1.0f, 0f);
		pmvMatrix.multPMvMatrixf(lightMat, 0);
		
		shadow.loadIdentity();
		shadow.multMatrix(scale_bias_Mat);
		shadow.multMatrix(lightMat);
		shadowMat = shadow.getMatrix();
		
		updateBuffers();
	}
	
	public static void setupBuffers()
	{
		gl.glGenBuffers(uboCount, uboBuff);
		
		gl.glBindBuffer(GL4.GL_UNIFORM_BUFFER, uboBuff.get(0));
		gl.glBufferData(GL4.GL_UNIFORM_BUFFER, shadingUniBuffLen, null, GL4.GL_DYNAMIC_DRAW);
		
		gl.glBindBuffer(GL4.GL_UNIFORM_BUFFER, uboBuff.get(1));
		gl.glBufferData(GL4.GL_UNIFORM_BUFFER, lightMatUniBuffLen, null, GL4.GL_DYNAMIC_DRAW);
		
		gl.glBindBufferBase(GL4.GL_UNIFORM_BUFFER, shadingUniBuffIndex, uboBuff.get(0));
		gl.glBindBufferBase(GL4.GL_UNIFORM_BUFFER, lightMatUniBuffIndex, uboBuff.get(1));
	}
	
	public static void updateBuffers()
	{
		shadingUniBuff.clear();
		lightMatUniBuff.clear();
		
		shadingUniBuff.put(shadowMat);
		shadingUniBuff.put(position);
		shadingUniBuff.put(20, SHADOWMAP_WIDTH);
		shadingUniBuff.put(21, SHADOWMAP_HEIGHT);
		shadingUniBuff.rewind();
		
		lightMatUniBuff.put(lightMat);
		lightMatUniBuff.rewind();
		
		gl.glBindBuffer(GL4.GL_UNIFORM_BUFFER, uboBuff.get(0));
		gl.glBufferSubData(GL4.GL_UNIFORM_BUFFER, 0, shadingUniBuffLen, shadingUniBuff);
		
		gl.glBindBuffer(GL4.GL_UNIFORM_BUFFER, uboBuff.get(1));
		gl.glBufferSubData(GL4.GL_UNIFORM_BUFFER, 0, lightMatUniBuffLen, lightMatUniBuff);
	}
	
	public static void setPosition(float[] p)
	{
		position[0] = p[0];
		position[1] = p[1];
		position[2] = p[2];
	}
	
	public static void setTarget(float[] t)
	{
		target[0] = t[0];
		target[1] = t[1];
		target[2] = t[2];
	}
	
	public static void moveRight()
	{
		position[0] += moveRight[0];
		position[1] += moveRight[1];
		position[2] += moveRight[2];
	}
	
	public static void moveLeft()
	{
		position[0] += moveLeft[0];
		position[1] += moveLeft[1];
		position[2] += moveLeft[2];
	}
}
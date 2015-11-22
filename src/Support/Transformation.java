package Support;

import java.util.Stack;
import com.jogamp.opengl.math.Matrix4;


/**
 * @author Yu Sangzhuoyang
 * @version 3.14
 */
public class Transformation
{
	private Matrix4 matrix;
	private float[] homoCoordsIn;
	private float[] homoCoordsOut;
	
	private float rotationX;
	private float rotationY;
	private float rotationZ;
	
	private Stack<Matrix4> matrixStack;
	
	public Transformation()
	{
		matrix = new Matrix4();
		matrixStack = new Stack<Matrix4>();
		
		homoCoordsIn = new float[4];
		homoCoordsOut = new float[4];
		
		homoCoordsIn[0] = 0f;
		homoCoordsIn[1] = 0f;
		homoCoordsIn[2] = 0f;
		homoCoordsIn[3] = 1f;
		
		homoCoordsOut[0] = 0f;
		homoCoordsOut[1] = 0f;
		homoCoordsOut[2] = 0f;
		homoCoordsOut[3] = 1f;
		
		rotationX = 0f;
		rotationY = 0f;
		rotationZ = 0f;
	}
	
	public void loadIdentity()
	{
		matrix.loadIdentity();
	}
	
	public void translate(float x, float y, float z)
	{
		matrix.translate(x, y, z);
	}
	
	public void scale(float x, float y, float z)
	{
		matrix.scale(x, y, z);
	}
	
	public void rotate(float angel, float x, float y, float z)
	{
		matrix.rotate(angel, x, y, z);
	}
	
	public void rotateByStages(int segments)
	{
		matrix.rotate(rotationX / (float) segments, 1.0f, 0.0f, 0.0f);//ÒþÊ½×ª»»?
		matrix.rotate(rotationY / (float) segments, 0.0f, 1.0f, 0.0f);
		matrix.rotate(rotationZ / (float) segments, 0.0f, 0.0f, 1.0f);
	}
	
	public void addRotationDegreesX(float rx)
	{
		rotationX += rx;
	}
	
	public void addRotationDegreesY(float ry)
	{
		rotationY += ry;
	}
	
	public void addRotationDegreesZ(float rz)
	{
		rotationZ += rz;
	}
	
	public void resetRotationDegrees()
	{
		rotationX = 0f;
		rotationY = 0f;
		rotationZ = 0f;
	}
	
	public void applyTransformation(float[] in, float[] out)
	{
		homoCoordsIn[0] = in[0];
		homoCoordsIn[1] = in[1];
		homoCoordsIn[2] = in[2];
		homoCoordsIn[3] = 1f;
		
		matrix.multVec(homoCoordsIn, homoCoordsOut);
		
		out[0] = homoCoordsOut[0];
		out[1] = homoCoordsOut[1];
		out[2] = homoCoordsOut[2];
		
		homoCoordsOut[3] = 1f;
	}
	
	public void applyTransformation(Vertex in, Vertex out)
	{
		homoCoordsIn[0] = in.getX();
		homoCoordsIn[1] = in.getY();
		homoCoordsIn[2] = in.getZ();
		homoCoordsIn[3] = 1f;
		
		matrix.multVec(homoCoordsIn, homoCoordsOut);
		
		out.setPosition(homoCoordsOut[0], homoCoordsOut[1], homoCoordsOut[2]);
		
		homoCoordsOut[3] = 1f;
	}
	
	public void applyTransToAll(Vertex[] in, Vertex[] out)
	{
		for (int i = 0; i < in.length; i++)
		{
			applyTransformation(in[i], out[i]);
		}
	}
	
	public void applyTransToAll(float[] in, float[] out)
	{
		if (in.length % 3 != 0)
		{
			System.out.println("The length of the input array is not correct");
			
			return;
		}
		
		for (int n = 0; n < in.length / 3; n++)
		{
			homoCoordsIn[0] = in[n * 3 + 0];
			homoCoordsIn[1] = in[n * 3 + 1];
			homoCoordsIn[2] = in[n * 3 + 2];
			homoCoordsIn[3] = 1f;
			
			matrix.multVec(homoCoordsIn, homoCoordsOut);
			
			out[n * 3 + 0] = homoCoordsOut[0];
			out[n * 3 + 1] = homoCoordsOut[1];
			out[n * 3 + 2] = homoCoordsOut[2];
			
			homoCoordsOut[3] = 1f;
		}
	}
	
	public void pushMatrix()
	{
		Matrix4 matStore = new Matrix4();
		matStore.multMatrix(matrix);
		
		matrixStack.push(matStore);
	}
	
	public void popMatrix()
	{
		matrix = matrixStack.pop();
	}
	
	public void multMatrix(Transformation trans)
	{
		matrix.multMatrix(trans.getMatrix());
	}
	
	public Transformation clone()
	{
		Transformation newTrans = new Transformation();
		
		newTrans.multMatrix(this);
		newTrans.addRotationDegreesX(rotationX);
		newTrans.addRotationDegreesY(rotationY);
		newTrans.addRotationDegreesZ(rotationZ);
		
		return newTrans;
	}
	
	public float[] getMatrix()
	{
		return matrix.getMatrix();
	}
}
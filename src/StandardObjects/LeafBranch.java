package StandardObjects;

import Support.InfoLog;
import Support.Transformation;
import Support.Vertex;

import static Support.Resources.*;

import java.nio.FloatBuffer;
import java.util.ArrayDeque;
import java.util.Queue;

import com.jogamp.opengl.GL4;
import com.jogamp.opengl.util.GLBuffers;


/**
 * @author Yu Sangzhuoyang
 * @version 4.19
 */
public class LeafBranch extends Object3D
{
	private float width;
	private float length;
	private float height;
	
	private boolean smallLeaf;
	
	private int[] stripCount;
	private int[] firstOfStrip;
	
	private int instanceCount;
	
	/*private int verPosLoc;
	//private int tc_inLoc;
	//private int d_idLoc;
	private int modelMatLoc;*/
	
	private LeafBlade leaf;
	private SmallLeaf sleaf;
	
	private Queue<FloatBuffer> vertBuffQueue;
	
	public LeafBranch()
	{
		instanceCount = 0;
		
		vertices = new Vertex[10];
		vertBuffSize = vertices.length * 3;
		modelMat = new float[16];
		
		vaoBuff = GLBuffers.newDirectIntBuffer(1);
		vboBuff = GLBuffers.newDirectIntBuffer(1);
		
		vertBuffQueue = new ArrayDeque<FloatBuffer>();
		
		//verPosLoc = 0;
		//modelMatLoc = 5;
	}
	
	public void addLeafBranch(Transformation trans, float w, float l, float h)
	{
		width = w;
		length = l;
		height = h;
		
		transMove = trans.clone();
		
		setupVertices();
	}

	public void generate()
	{
		initViewProgram();
		setupBuffers();
	}
	
	public void setModelMatrix(float[] m)
	{
		modelMat = m;
	}
	
	public void attach(LeafBlade l)
	{
		leaf = l;
		
		smallLeaf = false;
	}
	
	public void attach(SmallLeaf l)
	{
		sleaf = l;
		
		smallLeaf = true;
	}
	
	protected void initViewProgram()
	{
		//create shaders for rendering leaf branch
		int verShader_bran = gl.glCreateShader(GL4.GL_VERTEX_SHADER);
		int fraShader_bran = gl.glCreateShader(GL4.GL_FRAGMENT_SHADER);
		
		String[] vs_source_bran = 
			{
				"#version 430										\n" + 
				"													\n" + 
				"layout (std140, binding = 7) uniform VPMatBlock	\n" + 
				"{													\n" + 
				"	mat4 viewMat;									\n" + 
				"	mat4 proMat;									\n" + 
				"} vpMatrix;										\n" + 
				"													\n" + 
				"layout (location = 5) uniform mat4 modelMat;		\n" + 
				"layout (location = 0) in vec4 position;			\n" + 
				//"layout (location = 1) in vec2 tc_in;				\n" + 
				//mind that the array variable should not use qualifiers like 
				//"layout (location = 0)", otherwise it will be regarded as a single 
				//element of that array.
				//"out vec2 tc;										\n" + 
				"void main(void)									\n" + 
				"{													\n" + 
				"	gl_Position = vpMatrix.proMat * vpMatrix.viewMat * modelMat * position;	\n" + 
				//"	tc = tc_in;										\n" + 
				"}"
			};//System.out.println("\n" + "branch: \n" + vs_source_bran[0] + "\n");
		
		gl.glShaderSource(verShader_bran, 1, vs_source_bran, null);
		gl.glCompileShader(verShader_bran);
		InfoLog.printShaderInfoLog(verShader_bran);
		
		String[] fs_source_bran = 
			{
				// Output to the framebuffer
				"#version 430										\n" + 
				//"layout (binding = 0) uniform sampler2D tex_object;	\n" + 
				//"in vec2 tc;										\n" + 
				"out vec4 color;									\n" + 
				"void main(void)									\n" + 
				"{													\n" + 
				//"	color = texture(tex_object, tc);				\n" + 
				"	color = vec4(0.3, 0.22, 0.18, 1.0);				\n" + 
				"}"
			};//System.out.println(fs_source_bran[0]);
		
		gl.glShaderSource(fraShader_bran, 1, fs_source_bran, null);
		gl.glCompileShader(fraShader_bran);
		InfoLog.printShaderInfoLog(fraShader_bran);
		
		viewProgram = gl.glCreateProgram();
		gl.glAttachShader(viewProgram, verShader_bran);
		gl.glAttachShader(viewProgram, fraShader_bran);
		gl.glLinkProgram(viewProgram);
		gl.glValidateProgram(viewProgram);
		InfoLog.printProgramInfoLog(viewProgram);
		
		gl.glDeleteShader(verShader_bran);
		gl.glDeleteShader(fraShader_bran);
	}
	
	protected void setupVertices()
	{
		//setup vertices for rendering leaf branch
		float[] cubeCoords = {
				-width / 2f, height, length / 2f, 
				-width / 2f, 0f, length / 2f, 
				width / 2f, height, length / 2f, 
				width / 2f, 0f, length / 2f, 
				width / 2f, height, -length / 2f, 
				width / 2f, 0f, -length / 2f, 
				-width / 2f, height, -length / 2f, 
				-width / 2f, 0f, -length / 2f, 
				-width / 2f, height, length / 2f, 
				-width / 2f, 0f, length / 2f
				};
		
		/*vertices[0] = new Vertex(-width / 2f, height, length / 2f, 0);
		vertices[1] = new Vertex(-width / 2f, 0f, length / 2f, 1);
		vertices[2] = new Vertex(width / 2f, height, length / 2f, 2);
		vertices[3] = new Vertex(width / 2f, 0f, length / 2f, 3);
		vertices[4] = new Vertex(width / 2f, height, -length / 2f, 4);
		vertices[5] = new Vertex(width / 2f, 0f, -length / 2f, 5);
		vertices[6] = new Vertex(-width / 2f, height, -length / 2f, 6);
		vertices[7] = new Vertex(-width / 2f, 0f, -length / 2f, 7);
		vertices[8] = new Vertex(-width / 2f, height, length / 2f, 8);
		vertices[9] = new Vertex(-width / 2f, 0f, length / 2f, 9);*/
		
		transMove.applyTransToAll(cubeCoords, cubeCoords);
		
		vertBuff = GLBuffers.newDirectFloatBuffer(cubeCoords);
		vertBuffQueue.add(vertBuff);
		
		instanceCount++;
		
		if (smallLeaf)
		{
			if (height > 0.4)
			{
				transMove.pushMatrix();
				transMove.translate(0f, height / 2, 0f);
				transMove.rotate(-1.2f + (float) Math.random() * 0.4f - 0.2f, 1.0f, 0f, 0f);
				sleaf.addLeaf(transMove, 0.2f, 0.4f);
				transMove.popMatrix();
			}
			
			transMove.rotate(1.6f, 0f, 1.0f, 0f);
			transMove.translate(0f, height, 0f);
			transMove.rotate(-1.2f + (float) Math.random() * 0.4f - 0.2f, 1.0f, 0f, 0f);
			sleaf.addLeaf(transMove, 0.2f, 0.4f);
			transMove.rotate(3.14f, 0.0f, 1.0f, 0f);
			transMove.rotate(-2.5f + (float) Math.random() * 0.4f - 0.2f, 1.0f, 0f, 0f);
			sleaf.addLeaf(transMove, 0.2f, 0.4f);
		}
		else
		{
			if (height > 0.4)
			{
				transMove.pushMatrix();
				transMove.translate(0f, height / 2, 0f);
				transMove.rotate(-1.2f + (float) Math.random() * 0.4f - 0.2f, 1.0f, 0f, 0f);
				leaf.addLeaf(transMove, 0.2f, 0.4f);
				transMove.popMatrix();
			}
			
			transMove.rotate(1.6f, 0f, 1.0f, 0f);
			transMove.translate(0f, height, 0f);
			transMove.rotate(-1.2f + (float) Math.random() * 0.4f - 0.2f, 1.0f, 0f, 0f);
			leaf.addLeaf(transMove, 0.2f, 0.4f);
			transMove.rotate(3.14f, 0.0f, 1.0f, 0f);
			transMove.rotate(-2.5f + (float) Math.random() * 0.4f - 0.2f, 1.0f, 0f, 0f);
			leaf.addLeaf(transMove, 0.2f, 0.4f);
		}
	}
	
	protected void setupBuffers()
	{
		//setup buffers for drawing leaf branch
		gl.glGenVertexArrays(1, vaoBuff);
		gl.glBindVertexArray(vaoBuff.get(0));
		
		stripCount = new int[instanceCount];
		firstOfStrip = new int[instanceCount];
		
		for (int i = 0; i < instanceCount; i++)
		{
			stripCount[i] = 10;
			firstOfStrip[i] = i * 10;
		}
		
		gl.glGenBuffers(1, vboBuff);
		gl.glBindBuffer(GL4.GL_ARRAY_BUFFER, vboBuff.get(0));
		gl.glBufferData(GL4.GL_ARRAY_BUFFER, instanceCount * vertBuffSize * Float.SIZE / 8, null, GL4.GL_STATIC_DRAW);
		
		int count = 0;
		
		for (FloatBuffer vert : vertBuffQueue)
		{
			gl.glBufferSubData(GL4.GL_ARRAY_BUFFER, count * vertBuffSize * Float.SIZE / 8, vertBuffSize * Float.SIZE / 8, vert);
			count++;
		}
		
		gl.glVertexAttribPointer(verPosLoc, 3, GL4.GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(verPosLoc);
	}
	
	public void renderDepth()
	{
		if (instanceCount > 0)
		{
			
		}
	}
	
	public void renderView()
	{
		if (instanceCount > 0)
		{
			//Draw leaf branch
			gl.glUseProgram(viewProgram);
			
			gl.glUniformMatrix4fv(modelMatLoc, 1, false, modelMat, 0);
			
			gl.glBindVertexArray(vaoBuff.get(0));
			gl.glMultiDrawArrays(GL4.GL_TRIANGLE_STRIP, firstOfStrip, 0, stripCount, 0, instanceCount);
		}
	}
	
	public void destroy()
	{
		
	}
}
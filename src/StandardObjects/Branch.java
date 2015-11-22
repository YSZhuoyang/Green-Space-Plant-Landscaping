package StandardObjects;

import Support.Face;
import Support.InfoLog;
import Support.Transformation;
import Support.Vertex;
import static Support.Resources.*;

import java.io.File;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayDeque;
import java.util.Queue;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;

import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.texture.awt.AWTTextureIO;

//import static javax.media.opengl.GL4.GL_CULL_FACE;
//import static javax.media.opengl.GL4.GL_FRONT;


/**
 * @author Yu Sangzhuoyang
 * @version 5.10
 */
public class Branch extends Object3D
{
	private int row_subdivision;
	private int column_subdivision;
	
	private Vertex[] sect;
	private Face[] faces;
	private int[] index;
	
	private float underRadius;
	private float radiusChangeRate;
	private float length;
	
	/*private int verPosLoc = 0;
	private int tc_inLoc = 1;
	private int nor_inLoc = 2;
	private int d_idLoc = 10;
	private int modelMatLoc = 5;*/
	
	private int instanceCount;
	
	private boolean withLeaf;
	private LeafBranch leafBranch;
	
	private IntBuffer indirectCmdBuff;
	
	private Queue<IntBuffer> indirectCmdQueue;
	private Queue<IntBuffer> indexBuffQueue;
	private Queue<FloatBuffer> vertBuffQueue;
	private Queue<FloatBuffer> normBuffQueue;
	private Queue<FloatBuffer> texBuffQueue;
	
	public Branch()
	{
		row_subdivision = 10;
		column_subdivision = 4;
		
		sect = new Vertex[row_subdivision + 1];
		vertices = new Vertex[(row_subdivision + 1) * (column_subdivision + 1)];
		faces = new Face[row_subdivision * 2 * column_subdivision];
		
		modelMat = new float[16];
		
		withLeaf = false;
		
		vboCount = 6;
		vaoBuff = GLBuffers.newDirectIntBuffer(1);
		vboBuff = GLBuffers.newDirectIntBuffer(vboCount);
		
		vertBuffSize = vertices.length * 3;
		indexBuffSize = 2 * (row_subdivision + 1) * column_subdivision;
		texBuffSize = vertices.length * 2;
		
		indirectCmdQueue = new ArrayDeque<IntBuffer>();
		indexBuffQueue = new ArrayDeque<IntBuffer>();
		vertBuffQueue = new ArrayDeque<FloatBuffer>();
		normBuffQueue = new ArrayDeque<FloatBuffer>();
		texBuffQueue = new ArrayDeque<FloatBuffer>();
		
		instanceCount = 0;
		
		/*verPosLoc = 0;
		tc_inLoc = 1;
		nor_inLoc = 2;
		d_idLoc = 10;
		modelMatLoc = 5;*/
	}
	
	public void addBranch(Transformation trans, float rUpper, float rUnder, float l, boolean leaf)
	{
		transMove = trans;
		transScale = new Transformation();
		
		underRadius = rUnder;
		radiusChangeRate = (rUnder - rUpper) / column_subdivision / rUnder;
		
		length = l;
		underRadius = rUnder;
		radiusChangeRate = (rUnder - rUpper) / column_subdivision / rUnder;
		
		for (int n = 0; n < sect.length; n++)
		{
			sect[n] = new Vertex(n);
		}
		
		for (int n = 0; n < vertices.length; n++)
		{
			vertices[n] = new Vertex(n);
		}
		
		index = new int[2 * (row_subdivision + 1) * column_subdivision];
		
		withLeaf = leaf;
		
		setupVertices();
		setupNormals();
		setupTexCoords();
	}
	
	public void generate()
	{
		initDepthProgram();
		initViewProgram();
		loadTexture();
		setupBuffers();
	}
	
	public void setModelMatrix(float[] m)
	{
		modelMat = m;
	}
	
	public void attach(LeafBranch lb)
	{
		leafBranch = lb;
	}
	
	protected void initDepthProgram()
	{
		int verShader = gl.glCreateShader(GL4.GL_VERTEX_SHADER);
		int fraShader = gl.glCreateShader(GL4.GL_FRAGMENT_SHADER);
		
		String[] vs_source = 
			{
				"#version 430										\n" + 
				"													\n" + 
				"layout (std140, binding = 10) uniform lightMatBlock\n" + 
				"{													\n" + 
				"	mat4 lightMat;									\n" + 
				"} depthTrans;										\n" + 
				"													\n" + 
				"layout (location = 5) uniform mat4 modelMat;		\n" + 
				"layout (location = 0) in vec4 position;			\n" + 
				"													\n" + 
				"void main(void)									\n" + 
				"{													\n" + 
				"	gl_Position = depthTrans.lightMat * modelMat * position;	\n" + 
				"}"
			};//System.out.println("\n" + "grass: \n" + vs_source[0] + "\n");
		
		gl.glShaderSource(verShader, 1, vs_source, null);
		gl.glCompileShader(verShader);
		InfoLog.printShaderInfoLog(verShader);
		
		String[] fs_source = 
			{
				// Output to the framebuffer
				"#version 430										\n" + 
				"out vec4 color;									\n" + 
				"													\n" + 
				"void main(void)									\n" + 
				"{													\n" + 
				"	color = vec4(gl_FragCoord.z);					\n" + 
				"}"
			};//System.out.println(fs_source[0]);
		
		gl.glShaderSource(fraShader, 1, fs_source, null);
		gl.glCompileShader(fraShader);
		InfoLog.printShaderInfoLog(fraShader);
		
		depthProgram = gl.glCreateProgram();
		gl.glAttachShader(depthProgram, verShader);
		gl.glAttachShader(depthProgram, fraShader);
		gl.glLinkProgram(depthProgram);
		gl.glValidateProgram(depthProgram);
		InfoLog.printProgramInfoLog(depthProgram);
		
		gl.glDeleteShader(verShader);
		gl.glDeleteShader(fraShader);
	}
	
	protected void initViewProgram()
	{
		int verShader = gl.glCreateShader(GL4.GL_VERTEX_SHADER);
		int fraShader = gl.glCreateShader(GL4.GL_FRAGMENT_SHADER);
		
		String[] vs_source = 
			{
				"#version 430										\n" + 
				"													\n" + 
				"layout (std140, binding = 7) uniform VPMatBlock	\n" + 
				"{													\n" + 
				"	mat4 viewMat;									\n" + 
				"	mat4 proMat;									\n" + 
				"} vpMatrix;										\n" + 
				"													\n" + 
				"layout (std140, binding = 9) uniform lightPosBlock	\n" + 
				"{													\n" + 
				"	mat4 shadowMat;									\n" + 
				"	vec3 light_pos;									\n" + 
				"	vec2 shadowMapSize;								\n" + 
				"} shading;											\n" + 
				"													\n" + 
				"layout (location = 5) uniform mat4 modelMat;		\n" + 
				"layout (location = 0) in vec4 position;			\n" + 
				"layout (location = 1) in vec3 normal;				\n" + 
				"layout (location = 2) in vec2 tc_in;				\n" + 
				"layout (location = 10) in uint draw_id;			\n" + 
				"													\n" + 
				"out VS_OUT											\n" + 
				"{													\n" + 
				"	vec3 N;											\n" + 
				"	vec3 L;											\n" + 
				"	vec3 V;											\n" + 
				"	vec4 shadowCoord;								\n" + 
				"} vs_out;											\n" + 
				"													\n" + 
				//mind that the array variable should not use qualifiers like 
				//"layout (location = 0)", otherwise it will be regarded as a single 
				//element of that array.
				"out vec2 tc;										\n" + 
				"													\n" + 
				"void main(void)									\n" + 
				"{													\n" + 
				"	vec4 P = vpMatrix.viewMat * modelMat * position;\n" + 
				"													\n" + 
				"	vs_out.N = mat3(vpMatrix.viewMat * modelMat) * normal;			\n" + 
				"	vs_out.L = mat3(vpMatrix.viewMat) * shading.light_pos - P.xyz;	\n" + 
				"	vs_out.V = -P.xyz;												\n" + 
				"	vs_out.shadowCoord = shading.shadowMat * modelMat * position;	\n" + 
				"													\n" + 
				"	gl_Position = vpMatrix.proMat * P;				\n" + 
				"	tc = tc_in;										\n" + 
				"}"
			};//System.out.println("\n" + "branch: \n" + vs_source[0] + "\n");
		
		gl.glShaderSource(verShader, 1, vs_source, null);
		gl.glCompileShader(verShader);
		InfoLog.printShaderInfoLog(verShader);
		
		String[] fs_source = 
			{
				// Output to the framebuffer
				"#version 430											\n" + 
				"														\n" + 
				"layout (std140, binding = 9) uniform lightPosBlock		\n" + 
				"{														\n" + 
				"	mat4 shadowMat;										\n" + 
				"	vec3 light_pos;										\n" + 
				"	vec2 shadowMapSize;									\n" + 
				"} shading;												\n" + 
				"														\n" + 
				"layout (binding = 0) uniform sampler2D tex_object;		\n" + 
				"layout (binding = 5) uniform sampler2DShadow shadow_tex;	\n" + 
                "in vec2 tc;											\n" + 
				"out vec4 color;										\n" + 
				"														\n" + 
				"in VS_OUT												\n" + 
				"{														\n" + 
				"	vec3 N;												\n" + 
				"	vec3 L;												\n" + 
				"	vec3 V;												\n" + 
				"	vec4 shadowCoord;									\n" + 
				"} fs_in;												\n" + 
				"														\n" + 
				"float PCF(vec4 centerCoord)							\n" + 
				"{														\n" + 
				"	float factor = 0.0;									\n" + 
				"	vec4 offsets;										\n" + 
				"	vec4 UVC;											\n" + 
				"														\n" + 
				"	for (int y = -1; y <= 1; y++)						\n" + 
				"	{													\n" + 
				"		for (int x = -1; x <= 1; x++)					\n" + 
				"		{												\n" + 
				"			offsets = vec4(x / shading.shadowMapSize.x * centerCoord.w, " + 
				"y / shading.shadowMapSize.y * centerCoord.w, 0.0, 0.0);\n" + 
				"			UVC = vec4(centerCoord + offsets);			\n" + 
				"														\n" + 
				"			factor += textureProj(shadow_tex, UVC);		\n" + 
				"		}												\n" + 
				"	}													\n" + 
				"														\n" + 
				"	factor = 0.2 + factor / 11.25;						\n" + 
				"														\n" + 
				"	return factor;										\n" + 
				"}														\n" + 
				"														\n" + 
				"void main(void)										\n" + 
				"{														\n" + 
				"	vec3 ambient_albedo = texture(tex_object, tc).rgb;	\n" + 
				"	vec3 diffuse_albedo = texture(tex_object, tc).rgb;	\n" + 
				"														\n" + 
				"	vec3 N = normalize(fs_in.N);						\n" + 
				"	vec3 L = normalize(fs_in.L);						\n" + 
				"	vec3 V = normalize(fs_in.V);						\n" + 
				"	vec3 R = reflect(-L, N);							\n" + 
				"														\n" + 
				"	vec3 ambient = 0.2 * ambient_albedo;				\n" + 
				"	vec3 diffuse = max(dot(N, L), 0.0) * diffuse_albedo;\n" + 
				"														\n" + 
				"	color = PCF(fs_in.shadowCoord) * vec4(diffuse, 1.0) + vec4(ambient, 1.0);	\n" + 
				"														\n" + 
				//"	float shadowed = textureProj(shadow_tex, fs_in.shadowCoord);	\n" + 
				//"	color = shadowed * vec4(diffuse, 0.0) + vec4(ambient, 1.0);	\n" + 
				"}"
			};//System.out.println(fs_source[0]);
		
		gl.glShaderSource(fraShader, 1, fs_source, null);
		gl.glCompileShader(fraShader);
		InfoLog.printShaderInfoLog(fraShader);
		
		viewProgram = gl.glCreateProgram();
		gl.glAttachShader(viewProgram, verShader);
		gl.glAttachShader(viewProgram, fraShader);
		gl.glLinkProgram(viewProgram);
		gl.glValidateProgram(viewProgram);
		InfoLog.printProgramInfoLog(viewProgram);
		
		gl.glDeleteShader(verShader);
		gl.glDeleteShader(fraShader);
	}
	
	protected void setupVertices()
	{
		vertBuff = GLBuffers.newDirectFloatBuffer(vertBuffSize);
		indexBuff = GLBuffers.newDirectIntBuffer(indexBuffSize);
		
		Transformation originTrans = transMove.clone();
		
		//Set coordinates.
		for (int i = 0; i < sect.length; i++)
		{
			sect[i].setPosition((float) (underRadius * Math.cos(i * 2 * Math.PI / row_subdivision)), 
								0f, 
								(float) (-underRadius * Math.sin(i * 2 * Math.PI / row_subdivision)));
			
			transMove.applyTransformation(sect[i], vertices[i]);
			
			vertBuff.put(vertices[i].getPosition());
		}
		
		float movement = length / column_subdivision;
		float radiusChange;
		
		//store vertices data
		for (int j = 1; j < column_subdivision + 1; j++)
		{
			radiusChange = 1 - radiusChangeRate * j;
			
			transScale.scale(radiusChange, 0f, radiusChange);
			
			transMove.rotateByStages(column_subdivision);
			transMove.translate(0f, movement, 0f);
			
			for (int i = 0; i < row_subdivision + 1; i++)
			{
				transScale.applyTransformation(sect[i], vertices[i + j * (row_subdivision + 1)]);
				transMove.applyTransformation(vertices[i + j * (row_subdivision + 1)], vertices[i + j * (row_subdivision + 1)]);
				
				vertBuff.put(vertices[i + j * (row_subdivision + 1)].getPosition());
			}
			
			if (withLeaf)
			{
				Transformation transLeaf;
				
				for (int i = 0; i < 2; i++)
				{
					transLeaf = transMove.clone();
					transLeaf.rotate(3f * i + (float) Math.random() * 3f, 0f, 1.0f, 0f);
					transLeaf.rotate(-0.8f - (float) Math.random() / 2f, 1.0f, 0f, 0f);
					transLeaf.translate(0f, 0f, -underRadius);
					leafBranch.addLeafBranch(transLeaf, 0.01f, 0.01f, 0.4f + ((float) Math.random() - 0.5f) * 0.4f);
				}
			}
			
			transScale.loadIdentity();
		}
		
		vertBuff.rewind();
		vertBuffQueue.add(vertBuff);
		
		int indexCount = 0;
		int offset = instanceCount / column_subdivision * vertBuffSize / 3;
		
		//store index data
		for (int j = 0; j < column_subdivision; j++)
		{
			for (int i = 0; i < row_subdivision + 1; i++)
			{
				index[indexCount++] = i + (j + 1) * (row_subdivision + 1) + offset;
				index[indexCount++] = i + j * (row_subdivision + 1) + offset;
			}
		}
		
		indexBuff.put(index);
		indexBuff.rewind();
		indexBuffQueue.add(indexBuff);
		
		int count = (row_subdivision + 1) * 2;;
		int primCount = 1;
		int firstIndex;
		int baseVertex = 0;
		int baseInstance;
		
		for (int n = 0; n < column_subdivision; n++)
		{
			firstIndex = instanceCount * count;
			baseInstance = instanceCount;
			
			int[] indirectCmd = {count, primCount, firstIndex, baseVertex, baseInstance};
			indirectCmdBuff = GLBuffers.newDirectIntBuffer(indirectCmd);
			indirectCmdQueue.add(indirectCmdBuff);
			
			instanceCount++;
		}
		
		withLeaf = false;
		
		transMove.resetRotationDegrees();
		transMove = originTrans;
	}
	
	protected void setupNormals()
	{
		normBuff = GLBuffers.newDirectFloatBuffer(vertBuffSize);
		
		//calculate face normal
		int count = 0;
		int offset = 0;
		int local_offset = -(instanceCount / column_subdivision - 1) * vertices.length;
		
		for (int j = 0; j < column_subdivision; j++)
		{
			for (int i = 0; i < row_subdivision * 2; i++)
			{
				count = j * row_subdivision * 2 + i;
				offset = count + 2 * j;
				
				if (count % 2 == 0)
				{
					faces[count] = new Face(indexBuff.get(offset) + local_offset, indexBuff.get(offset + 1) + local_offset, indexBuff.get(offset + 2) + local_offset);
				}
				else
				{
					faces[count] = new Face(indexBuff.get(offset + 1) + local_offset, indexBuff.get(offset) + local_offset, indexBuff.get(offset + 2) + local_offset);
				}
				
				faces[count].calculateFaceNorm(vertices);
			}
		}
		
		//calculate vertex normal
		for (int i = 0; i < vertices.length; i++)
		{
			vertices[i].calculateVertNorm(faces);
		}
		
		for (int i = 0; i < column_subdivision + 1; i++)
		{
			vertices[i * (row_subdivision + 1)].mergeNormals(vertices[(i + 1) * (row_subdivision + 1) - 1]);
		}
		
		for (int i = 0; i < vertices.length; i++)
		{
			normBuff.put(vertices[i].getNormal());
		}
		
		normBuff.rewind();
		normBuffQueue.add(normBuff);
	}
	
	protected void setupTexCoords()
	{
		//Set branch texture.
		texBuff = GLBuffers.newDirectFloatBuffer(texBuffSize);
		
		int count = 0;
		float y = 0;
		float x;
		
		for (int j = 0; j < column_subdivision + 1; j++)
		{
			y = (float) j * 1f / column_subdivision;
			
			for (int i = 0; i < row_subdivision + 1; i++)
			{
				count = j * (row_subdivision + 1) + i;
				x = (float) i / row_subdivision;
				
				vertices[count].setTexCoords(x, y);
				
				texBuff.put(vertices[count].getTexCoords());
			}
		}
		
		texBuff.rewind();
		texBuffQueue.add(texBuff);
	}
	
	protected void setupBuffers()
	{
		gl.glGenVertexArrays(1, vaoBuff);
		gl.glBindVertexArray(vaoBuff.get(0));
		
		//bind indirect buffer object
		gl.glGenBuffers(vboCount, vboBuff);
		
		gl.glBindBuffer(GL4.GL_DRAW_INDIRECT_BUFFER, vboBuff.get(0));
		gl.glBufferData(GL4.GL_DRAW_INDIRECT_BUFFER, instanceCount * 5 * Integer.SIZE / 8, null, GL4.GL_STATIC_DRAW);
		
		int count = 0;
		
		for (IntBuffer cmd : indirectCmdQueue)
		{
			gl.glBufferSubData(GL4.GL_DRAW_INDIRECT_BUFFER, count * 5 * Integer.SIZE / 8, 5 * Integer.SIZE / 8, cmd);
			count++;
		}
		
		//bind draw instance ID in the shader with a buffer object
		gl.glBindBuffer(GL4.GL_ARRAY_BUFFER, vboBuff.get(1));
		
		int[] draw_index = new int[instanceCount];
		for (int i = 0; i < instanceCount; i++)
		{
			draw_index[i] = i;
		}
		IntBuffer drawIndexBuff = GLBuffers.newDirectIntBuffer(draw_index);
		
		gl.glBufferData(GL4.GL_ARRAY_BUFFER, instanceCount * Integer.SIZE / 8, drawIndexBuff, GL4.GL_STATIC_DRAW);
		gl.glVertexAttribIPointer(d_idLoc, 1, GL4.GL_UNSIGNED_INT, 0, 0);
		gl.glVertexAttribDivisor(d_idLoc, 1);
		gl.glEnableVertexAttribArray(d_idLoc);
		
		//bind vertex data buffer object
		gl.glBindBuffer(GL4.GL_ARRAY_BUFFER, vboBuff.get(2));
		gl.glBufferData(GL4.GL_ARRAY_BUFFER, instanceCount / column_subdivision * vertBuffSize * Float.SIZE / 8, null, GL4.GL_STATIC_DRAW);
		
		count = 0;
		
		for (FloatBuffer vert : vertBuffQueue)
		{
			gl.glBufferSubData(GL4.GL_ARRAY_BUFFER, count * vertBuffSize * Float.SIZE / 8, vertBuffSize * Float.SIZE / 8, vert);
			count++;
		}
		
		//notice that the offset value is also calculated in bits, thus the size of 
		//float (32bits, that is 4 bytes per float value) should be taken into consideration
		gl.glVertexAttribPointer(verPosLoc, 3, GL4.GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(verPosLoc);
		
		//bind vertex index data buffer object
		gl.glBindBuffer(GL4.GL_ELEMENT_ARRAY_BUFFER, vboBuff.get(3));
		gl.glBufferData(GL4.GL_ELEMENT_ARRAY_BUFFER, instanceCount / column_subdivision * indexBuffSize * Integer.SIZE / 8, null, GL4.GL_STATIC_DRAW);
		
		count = 0;
		
		for (IntBuffer index : indexBuffQueue)
		{
			gl.glBufferSubData(GL4.GL_ELEMENT_ARRAY_BUFFER, count * indexBuffSize * Float.SIZE / 8, indexBuffSize * Integer.SIZE / 8, index);
			count++;
		}
		
		//bind texture coordinate data buffer object
		gl.glBindBuffer(GL4.GL_ARRAY_BUFFER, vboBuff.get(4));
		gl.glBufferData(GL4.GL_ARRAY_BUFFER, instanceCount / column_subdivision * texBuffSize * Float.SIZE / 8, null, GL4.GL_STATIC_DRAW);
		
		count = 0;
		
		for (FloatBuffer tex : texBuffQueue)
		{
			gl.glBufferSubData(GL4.GL_ARRAY_BUFFER, count * texBuffSize * Float.SIZE / 8, texBuffSize * Float.SIZE / 8, tex);
			count++;
		}
		
		gl.glVertexAttribPointer(tc_inLoc, 2, GL4.GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(tc_inLoc);
		
		//bind normal data buffer object
		gl.glBindBuffer(GL4.GL_ARRAY_BUFFER, vboBuff.get(5));
		gl.glBufferData(GL4.GL_ARRAY_BUFFER, instanceCount / column_subdivision * vertBuffSize * Float.SIZE / 8, null, GL4.GL_STATIC_DRAW);
		
		count = 0;
		
		for (FloatBuffer normal : normBuffQueue)
		{
			gl.glBufferSubData(GL4.GL_ARRAY_BUFFER, count * vertBuffSize * Float.SIZE / 8, vertBuffSize * Float.SIZE / 8, normal);
			count++;
		}
		
		gl.glVertexAttribPointer(nor_inLoc, 3, GL4.GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(nor_inLoc);
	}
	
	protected void loadTexture()
	{
		try
		{
			BufferedImage image = ImageIO.read(new File("images/tree.png"));
			texture = AWTTextureIO.newTexture(GLProfile.getDefault(), image, true);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public void renderDepth()
	{
		if (instanceCount > 0)
		{
			gl.glUseProgram(depthProgram);
			
			gl.glUniformMatrix4fv(modelMatLoc, 1, false, modelMat, 0);
			
			gl.glBindVertexArray(vaoBuff.get(0));
			gl.glBindBuffer(GL4.GL_DRAW_INDIRECT_BUFFER, vboBuff.get(0));
			gl.glMultiDrawElementsIndirect(GL4.GL_TRIANGLE_STRIP, GL4.GL_UNSIGNED_INT, null, instanceCount, 0);
		}
	}
	
	public void renderView()
	{
		if (instanceCount > 0)
		{
			/*if (!isCullBack)
			{
				gl.glEnable(GL_CULL_FACE);
				gl.glCullFace(GL_FRONT);
				
				isCullBack = true;
			}*/
			
			gl.glUseProgram(viewProgram);
			
			//gl.glActiveTexture(GL4.GL_TEXTURE5);
			//gl.glBindTexture(GL4.GL_TEXTURE_2D, 3);
			
			//used after calling the glUseProgram
			gl.glUniformMatrix4fv(modelMatLoc, 1, false, modelMat, 0);
			//gl.glUniform2f(shadowMapSizeLoc, SHADOWMAP_WIDTH, SHADOWMAP_HEIGHT);
			
			//this method will not be remembered by vao
			gl.glActiveTexture(GL4.GL_TEXTURE0);
			texture.bind(gl);
			
			gl.glBindVertexArray(vaoBuff.get(0));
			//The GL_DRAW_INDIRECT_BUFFER binding point is not part 
			//of vertex array object state. It's global context state. 
			//So I'll have to set this to the buffer I want to pull 
			//from before performing indirect operations from that buffer
			gl.glBindBuffer(GL4.GL_DRAW_INDIRECT_BUFFER, vboBuff.get(0));
			gl.glMultiDrawElementsIndirect(GL4.GL_TRIANGLE_STRIP, GL4.GL_UNSIGNED_INT, null, instanceCount, 0);
		}
	}
	
	public void destroy()
	{
		//gl.glDeleteVertexArrays(1, vaoBuff);
		//gl.glDeleteProgram(viewProgram);
	}
}
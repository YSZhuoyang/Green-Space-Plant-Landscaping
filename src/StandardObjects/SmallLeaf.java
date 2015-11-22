package StandardObjects;

import Support.Face;
import Support.InfoLog;
import Support.Transformation;
import Support.Vertex;
import static Support.Resources.*;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.ArrayDeque;
import java.util.Queue;

import javax.imageio.ImageIO;

import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.texture.awt.AWTTextureIO;


/**
 * @author Yu Sangzhuoyang
 * @version 5.29
 */
public class SmallLeaf extends Object3D
{
	private float width;
	private float length;
	
	private int[] first;
	private int[] count;
	private Face face;
	
	/*private int verPosLoc;
	private int nor_inLoc;
	private int tc_inLoc;
	private int modelMatLoc;*/
	
	private int instanceCount;
	
	private Queue<FloatBuffer> vertBuffQueue;
	private Queue<FloatBuffer> normBuffQueue;
	private Queue<FloatBuffer> texBuffQueue;
	
	public SmallLeaf()
	{
		vertices = new Vertex[4];
		modelMat = new float[16];
		
		face = new Face(0, 1, 2);
		
		vertBuffSize = vertices.length * 3;
		texBuffSize = vertices.length * 2;
		
		vboCount = 3;
		
		vaoBuff = GLBuffers.newDirectIntBuffer(1);
		vboBuff = GLBuffers.newDirectIntBuffer(vboCount);
		
		vertBuffQueue = new ArrayDeque<FloatBuffer>();
		normBuffQueue = new ArrayDeque<FloatBuffer>();
		texBuffQueue = new ArrayDeque<FloatBuffer>();
		
		/*verPosLoc = 0;
		nor_inLoc = 1;
		tc_inLoc = 2;
		modelMatLoc = 5;*/
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
				"out vec2 tc;										\n" + 
				"													\n" + 
				"out VS_OUT											\n" + 
				"{													\n" + 
				"	vec3 N;											\n" + 
				"	vec3 L;											\n" + 
				"	vec3 V;											\n" + 
				"	vec4 shadowCoord;								\n" + 
				"} vs_out;											\n" + 
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
				"	tc = tc_in;										\n" + 
				"													\n" + 
				"	gl_Position = vpMatrix.proMat * P;				\n" + 
				"}"
			};
		
		gl.glShaderSource(verShader, 1, vs_source, null);
		gl.glCompileShader(verShader);
		InfoLog.printShaderInfoLog(verShader);
		
		String[] fs_source = 
			{
				// Output to the framebuffer
				"#version 430										\n" + 
				"layout (binding = 0) uniform sampler2D tex_object;	\n" + 
				"layout (binding = 5) uniform sampler2DShadow shadow_tex;	\n" + 
				"in vec2 tc;										\n" + 
				"out vec4 color;									\n" + 
				"													\n" + 
				"in VS_OUT											\n" + 
				"{													\n" + 
				"	vec3 N;											\n" + 
				"	vec3 L;											\n" + 
				"	vec3 V;											\n" + 
				"	vec4 shadowCoord;								\n" + 
				"} fs_in;											\n" + 
				"													\n" + 
				"void main(void)									\n" + 
				"{													\n" + 
				"	vec4 baseColor = texture(tex_object, tc);		\n" + 
				"													\n" + 
				"	if (baseColor.a < 0.5)							\n" + 
				"		discard;									\n" + 
				"													\n" + 
				"	vec3 ambient_albedo = baseColor.rgb;			\n" + 
				"	vec3 diffuse_albedo = baseColor.rgb;			\n" + 
				//"	vec3 specular_albedo = vec3(0.4);				\n" + 
				//"	float specular_power = 1024.0;					\n" + 
				"													\n" + 
				"	vec3 N = normalize(fs_in.N);						\n" + 
				"	vec3 L = normalize(fs_in.L);						\n" + 
				"	vec3 V = normalize(fs_in.V);						\n" + 
				"	vec3 R = reflect(-L, N);							\n" + 
				"														\n" + 
				"	vec3 ambient = 0.2 * ambient_albedo;				\n" + 
				"	vec3 diffuse = max(dot(N, L), 0.0) * diffuse_albedo;\n" + 
				//"	vec3 specular = pow(max(dot(R, V), 0.0), specular_power) * specular_albedo;	\n" + 
				"														\n" + 
				"	float shadowed = textureProj(shadow_tex, fs_in.shadowCoord);	\n" + 
				"	color = shadowed * vec4(diffuse, 1.0) + vec4(ambient, 0.0);		\n" + // + specular
				"}"
			};
		
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
	
	public void addLeaf(Transformation trans, float w, float l)
	{
		width = w;
		length = l;
		
		transMove = trans.clone();
		
		setupVertices();
		setupTexCoords();
		setupNormals();
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
	
	protected void setupVertices()
	{
		vertBuff = GLBuffers.newDirectFloatBuffer(vertBuffSize);
		
		vertices[0] = new Vertex(-0.15f, 0.3f, 0f, 0);
		vertices[1] = new Vertex(-0.15f, 0f, 0f, 1);
		vertices[2] = new Vertex(0.15f, 0.3f, 0f, 2);
		vertices[3] = new Vertex(0.15f, 0f, 0f, 3);
		
		transMove.applyTransToAll(vertices, vertices);
		
		for (int n = 0; n < vertices.length; n++)
		{
			vertBuff.put(vertices[n].getPosition());
		}
		
		vertBuff.rewind();
		vertBuffQueue.add(vertBuff);
		
		instanceCount++;
	}
	
	protected void setupNormals()
	{
		normBuff = GLBuffers.newDirectFloatBuffer(vertBuffSize);
		
		face.calculateFaceNorm(vertices);
		
		for (int i = 0; i < vertices.length; i++)
		{
			vertices[i].setNormal(face.getNormal());
			
			normBuff.put(vertices[i].getNormal());
		}
		
		normBuff.rewind();
		normBuffQueue.add(normBuff);
	}
	
	protected void setupTexCoords()
	{
		texBuff = GLBuffers.newDirectFloatBuffer(texBuffSize);
		
		vertices[0].setTexCoords(new float[]{0f, 1f});
		vertices[1].setTexCoords(new float[]{0f, 0f});
		vertices[2].setTexCoords(new float[]{1f, 1f});
		vertices[3].setTexCoords(new float[]{1f, 0f});
		
		for (int n = 0; n < vertices.length; n++)
		{
			texBuff.put(vertices[n].getTexCoords());
		}
		
		texBuff.rewind();
		texBuffQueue.add(texBuff);
	}
	
	protected void setupBuffers()
	{
		first = new int[instanceCount];
		count = new int[instanceCount];
		
		for (int i = 0; i < instanceCount; i++)
		{
			first[i] = i * vertBuffSize / 3;
			count[i] = vertBuffSize / 3;
		}
		
		gl.glGenVertexArrays(1, vaoBuff);
		gl.glBindVertexArray(vaoBuff.get(0));
		
		gl.glGenBuffers(vboCount, vboBuff);
		//bind vertex data buffer object
		gl.glBindBuffer(GL4.GL_ARRAY_BUFFER, vboBuff.get(0));
		gl.glBufferData(GL4.GL_ARRAY_BUFFER, instanceCount * vertBuffSize * Float.SIZE / 8, null, GL4.GL_STATIC_DRAW);
		//gl.glBufferData(GL4.GL_ARRAY_BUFFER, instanceCount * 3 * Float.SIZE / 8, null, GL4.GL_STATIC_DRAW);
		
		int count = 0;
		
		for (FloatBuffer vert : vertBuffQueue)
		{
			gl.glBufferSubData(GL4.GL_ARRAY_BUFFER, count * vertBuffSize * Float.SIZE / 8, vertBuffSize * Float.SIZE / 8, vert);
			//gl.glBufferSubData(GL4.GL_ARRAY_BUFFER, count * 3 * Float.SIZE / 8, 3 * Float.SIZE / 8, vert);
			count++;
		}
		
		//notice that the offset value is also calculated in bits, thus the size of 
		//float (32bits, that is 4 bytes per float value) should be taken into consideration
		gl.glVertexAttribPointer(verPosLoc, 3, GL4.GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(verPosLoc);
		//gl.glVertexAttribDivisor(verPosLoc, 1);
		
		//bind texture coordinate data buffer object
		gl.glBindBuffer(GL4.GL_ARRAY_BUFFER, vboBuff.get(1));
		gl.glBufferData(GL4.GL_ARRAY_BUFFER, instanceCount * texBuffSize * Float.SIZE / 8, null, GL4.GL_STATIC_DRAW);
		
		count = 0;
		
		for (FloatBuffer tex : texBuffQueue)
		{
			gl.glBufferSubData(GL4.GL_ARRAY_BUFFER, count * texBuffSize * Float.SIZE / 8, texBuffSize * Float.SIZE / 8, tex);
			count++;
		}
		
		gl.glVertexAttribPointer(tc_inLoc, 2, GL4.GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(tc_inLoc);
		//gl.glVertexAttribDivisor(tc_inLoc, 1);
		
		//bind normal data buffer object
		gl.glBindBuffer(GL4.GL_ARRAY_BUFFER, vboBuff.get(2));
		gl.glBufferData(GL4.GL_ARRAY_BUFFER, instanceCount * vertBuffSize * Float.SIZE / 8, null, GL4.GL_STATIC_DRAW);
		
		count = 0;
		
		for (FloatBuffer normal : normBuffQueue)
		{
			gl.glBufferSubData(GL4.GL_ARRAY_BUFFER, count * vertBuffSize * Float.SIZE / 8, vertBuffSize * Float.SIZE / 8, normal);
			count++;
		}
		
		gl.glVertexAttribPointer(nor_inLoc, 3, GL4.GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(nor_inLoc);
		//gl.glVertexAttribDivisor(nor_inLoc, 1);
	}
	
	protected void loadTexture()
	{
		try
		{
			BufferedImage image;
			
			if (color == Color.GREEN)
			{
				image = ImageIO.read(new File("images/smallleaf_green.png"));
			}
			else
			{
				image = ImageIO.read(new File("images/smallleaf_purple.png"));
			}
			
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
			gl.glMultiDrawArrays(GL4.GL_TRIANGLE_STRIP, first, 0, count, 0, instanceCount);
		}
	}
	
	public void renderView()
	{
		if (instanceCount > 0)
		{
			//Draw leaf
			gl.glUseProgram(viewProgram);
			
			//used after calling the glUseProgram
			gl.glUniformMatrix4fv(modelMatLoc, 1, false, modelMat, 0);
			
			//this method will not be remembered by vao
			gl.glActiveTexture(GL4.GL_TEXTURE0);
			texture.bind(gl);
			
			gl.glBindVertexArray(vaoBuff.get(0));
			gl.glMultiDrawArrays(GL4.GL_TRIANGLE_STRIP, first, 0, count, 0, instanceCount);
		}
	}
	
	public void destroy()
	{
		
	}
}
package StandardObjects;

import static Support.Resources.gl;
import Support.InfoLog;
import Support.Transformation;
import Support.Vertex;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.imageio.ImageIO;

import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.math.Matrix4;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureData;
import com.jogamp.opengl.util.texture.TextureIO;
import com.jogamp.opengl.util.texture.awt.AWTTextureIO;


/**
 * @author Yu Sangzhuoyang
 * @version 6.13
 */
public class Object3D
{
	protected int verPosLoc = 0;
	protected int nor_inLoc = 1;
	protected int tc_inLoc = 2;
	protected int d_idLoc = 10;
	protected int modelMatLoc = 5;
	
	private int imageSize;
	private int imageWidth;
	protected int vertBuffSize;
	protected int normBuffSize;
	protected int indexBuffSize;
	protected int texBuffSize;
	protected int vboCount;
	
	protected Matrix4 modelMatrix;
	protected float[] modelMat;
	protected Vertex[] vertices;
	
	protected IntBuffer vaoBuff;
	protected IntBuffer vboBuff;
	protected IntBuffer indexBuff;
	protected FloatBuffer vertBuff;
	protected FloatBuffer normBuff;
	protected FloatBuffer texBuff;
	
	private Buffer texDataBuff;
	private IntBuffer texObjBuff;
	
	protected Transformation transMove;
	protected Transformation transScale;
	
	protected Texture texture;
	
	protected int depthProgram;
	protected int viewProgram;
	
	public Object3D()
	{
		vboCount = 3;
		
		vaoBuff = GLBuffers.newDirectIntBuffer(1);
		vboBuff = GLBuffers.newDirectIntBuffer(vboCount);
		texObjBuff = GLBuffers.newDirectIntBuffer(1);
		
		modelMatrix = new Matrix4();
		modelMat = modelMatrix.getMatrix();
		
		/*verPosLoc = 0;
		nor_inLoc = 1;
		tc_inLoc = 2;
		modelMatLoc = 5;*/
	}
	
	public void initialize()
	{
		initDepthProgram();
		initViewProgram();
		setupBuffers();
	}
	
	public void setPosition(float x, float y, float z)
	{
		transMove.translate(x, y, z);
	}
	
	public void setRotation(float angel, float x, float y, float z)
	{
		transMove.rotate(angel, x, y, z);
	}
	
	public void setParent(Object3D parent)
	{
		parent.setChild(this);
	}
	
	public void setChild(Object3D child)
	{
		child.attach(transMove);
		//child.setupVertices();
	}
	
	public void attach(Transformation trans)
	{
		transMove.multMatrix(trans);
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
				"#version 430											\n" + 
				"layout (binding = 0) uniform sampler2D tex_object;		\n" + 
				"layout (binding = 5) uniform sampler2DShadow shadow_tex;	\n" + 
				"in vec2 tc;											\n" + 
				"out vec4 color;										\n" + 
				"														\n" + 
				"layout (std140, binding = 9) uniform lightPosBlock		\n" + 
				"{														\n" + 
				"	mat4 shadowMat;										\n" + 
				"	vec3 light_pos;										\n" + 
				"	vec2 shadowMapSize;									\n" + 
				"} shading;												\n" + 
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
				"	vec4 baseColor = texture(tex_object, tc);			\n" + 
				"														\n" + 
				"	if (baseColor.a < 0.5)								\n" + 
				"		discard;										\n" + 
				"														\n" + 
				"	vec3 ambient_albedo = baseColor.rgb;				\n" + //vec3(0.6, 0.4, 0.3)
				"	vec3 diffuse_albedo = baseColor.rgb;				\n" + //vec3(0.6, 0.4, 0.3)
				//"	vec3 specular_albedo = vec3(0.4);					\n" + 
				//"	float specular_power = 1024.0;						\n" + 
				"														\n" + 
				"	vec3 N = normalize(fs_in.N);						\n" + 
				"	vec3 L = normalize(fs_in.L);						\n" + 
				"	vec3 V = normalize(fs_in.V);						\n" + 
				"	vec3 R = reflect(-L, N);							\n" + 
				"														\n" + 
				"	vec3 ambient = 0.2 * ambient_albedo;				\n" + 
				"	vec3 diffuse = max(dot(N, L), 0.0) * diffuse_albedo;\n" + 
				//"	vec3 specular = pow(max(dot(R, V), 0.0), specular_power) * specular_albedo;	\n" + 
				"														\n" + 
				//"	float shadowed = textureProj(shadow_tex, fs_in.shadowCoord);	\n" + 
				"	color = PCF(fs_in.shadowCoord) * vec4(diffuse, 1.0) + vec4(ambient, 1.0);	\n" + 
				//"	color = shadowed * vec4(diffuse, 1.0) + vec4(ambient, 0.0);		\n" + // + specular
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
	
	public void setModelMatrix(float[] m)
	{
		modelMat = m;
	}
	
	public void translate(float x, float y, float z)
	{
		modelMatrix.loadIdentity();
		//m x transMat
		modelMatrix.translate(x, y, z);
		modelMatrix.multMatrix(modelMat);
		
		modelMat = modelMatrix.getMatrix().clone();
	}
	
	public void rotate(float angle, float x, float y, float z)
	{
		modelMatrix.loadIdentity();
		modelMatrix.multMatrix(modelMat);
		//m x rotMat
		modelMatrix.rotate(angle, x, y, z);
		
		modelMat = modelMatrix.getMatrix().clone();
	}
	
	public void setVertCoords(FloatBuffer v, int size)
	{
		vertBuff = v;
		
		vertBuffSize = size;
	}
	
	public void setNormals(FloatBuffer vn, int size)
	{
		normBuff = vn;
		
		normBuffSize = size;
	}
	
	public void setTextCoords(FloatBuffer vt, int size)
	{
		texBuff = vt;
		
		texBuffSize = size;
	}
	
	protected void setupBuffers()
	{
		//generate vao and vbo
		gl.glGenVertexArrays(1, vaoBuff);
		gl.glBindVertexArray(vaoBuff.get(0));
		
		gl.glGenBuffers(vboCount, vboBuff);
		
		//bind the vertex position buffer
		gl.glBindBuffer(GL4.GL_ARRAY_BUFFER, vboBuff.get(0));
		gl.glBufferData(GL4.GL_ARRAY_BUFFER, vertBuffSize * Float.SIZE / 8, vertBuff, GL4.GL_STATIC_DRAW);
		gl.glVertexAttribPointer(verPosLoc, 3, GL4.GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(verPosLoc);
		
		//bind the vertex normal buffer
		gl.glBindBuffer(GL4.GL_ARRAY_BUFFER, vboBuff.get(1));
		gl.glBufferData(GL4.GL_ARRAY_BUFFER, normBuffSize * Float.SIZE / 8, normBuff, GL4.GL_STATIC_DRAW);
		gl.glVertexAttribPointer(nor_inLoc, 3, GL4.GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(nor_inLoc);
		
		//bind the texCoord buffer
		gl.glBindBuffer(GL4.GL_ARRAY_BUFFER, vboBuff.get(2));
		gl.glBufferData(GL4.GL_ARRAY_BUFFER, texBuffSize * Float.SIZE / 8, texBuff, GL4.GL_STATIC_DRAW);
		gl.glVertexAttribPointer(tc_inLoc, 2, GL4.GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(tc_inLoc);
	}
	
	public void setupTextures(String textureFilePath)
	{
		String fileDir = textureFilePath;
		TextureData texData = null;
		
		try
		{
			texData = TextureIO.newTextureData(gl.getGLProfile(), 
					new File(fileDir), false, TextureIO.JPG);
			
			texDataBuff = texData.getBuffer();
			texDataBuff.rewind();
			
			//notice that the image used must be square
			imageSize = texData.getHeight();
			imageWidth = texData.getWidth();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		gl.glGenTextures(1, texObjBuff);
		gl.glBindTexture(GL4.GL_TEXTURE_2D, texObjBuff.get(0));
		
		gl.glTexStorage2D(GL4.GL_TEXTURE_2D, 0, GL4.GL_RGBA8, imageWidth, imageSize);
		gl.glTexImage2D(GL4.GL_TEXTURE_2D, 0, GL4.GL_RGBA8, imageWidth, imageSize, 
				0, texData.getPixelFormat(), texData.getPixelType(), texDataBuff);
		gl.glTexParameteri(GL4.GL_TEXTURE_2D, GL4.GL_TEXTURE_MAG_FILTER, GL4.GL_LINEAR);//GL_LINEAR_MIPMAP_LINEAR
		gl.glTexParameteri(GL4.GL_TEXTURE_2D, GL4.GL_TEXTURE_MIN_FILTER, GL4.GL_LINEAR);
	}
	
	public void loadTexture(String textureFilePath)
	{
		try
		{
			BufferedImage image = ImageIO.read(new File(textureFilePath));
			texture = AWTTextureIO.newTexture(GLProfile.getDefault(), image, true);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public void renderDepth()
	{
		gl.glUseProgram(depthProgram);
		
		//used after calling the glUseProgram
		gl.glUniformMatrix4fv(modelMatLoc, 1, false, modelMat, 0);
		
		gl.glBindVertexArray(vaoBuff.get(0));
		gl.glDrawArrays(GL4.GL_TRIANGLES, 0, vertBuffSize / 3);
	}
	
	public void renderView()
	{
		gl.glUseProgram(viewProgram);
		
		//used after calling the glUseProgram
		gl.glUniformMatrix4fv(modelMatLoc, 1, false, modelMat, 0);
		
		//this method will not be remembered by vao
		gl.glActiveTexture(GL4.GL_TEXTURE0);
		//gl.glBindTexture(GL4.GL_TEXTURE_2D, texObjBuff.get(0));
		texture.bind(gl);
		
		gl.glBindVertexArray(vaoBuff.get(0));
		gl.glDrawArrays(GL4.GL_TRIANGLES, 0, vertBuffSize / 3);
	}
	
	public void destroy() {}
}
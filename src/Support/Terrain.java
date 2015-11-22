package Support;

import StandardObjects.Object3D;

import static Support.Resources.*;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.IntBuffer;
import javax.imageio.ImageIO;

import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.texture.TextureData;
import com.jogamp.opengl.util.texture.TextureIO;
import com.jogamp.opengl.util.texture.awt.AWTTextureIO;


/**
 * @author Yu Sangzhuoyang
 * @version 4.19
 */
public class Terrain extends Object3D
{
	private int verPosLoc;
	private int nor_inLoc;
	private int tc_inLoc;
	
	private int imageSize;
	
	private Buffer texDataBuff;
	private IntBuffer texObjBuff;
	
	public Terrain()
	{
		vertices = new Vertex[4];
		
		vertBuffSize = vertices.length * 3;
		texBuffSize = vertices.length * 2;
		
		vboCount = 3;
		
		vaoBuff = GLBuffers.newDirectIntBuffer(1);
		vboBuff = GLBuffers.newDirectIntBuffer(vboCount);
		vertBuff = GLBuffers.newDirectFloatBuffer(vertBuffSize);
		normBuff = GLBuffers.newDirectFloatBuffer(vertBuffSize);
		texBuff = GLBuffers.newDirectFloatBuffer(texBuffSize);
		texObjBuff = GLBuffers.newDirectIntBuffer(1);
		
		verPosLoc = 0;
		nor_inLoc = 1;
		tc_inLoc = 2;
	}
	
	public void initialize()
	{
		//initDepthProgram();
		initViewProgram();
		setupVertices();
		setupNormals();
		setupTexCoords();
		setupBuffers();
		loadTexture();
		//setupTextures();
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
				"layout (location = 0) in vec4 position;			\n" + 
				"													\n" + 
				"void main(void)									\n" + 
				"{													\n" + 
				"	gl_Position = depthTrans.lightMat * position;	\n" + 
				"}"
			};System.out.println("\n" + "grass: \n" + vs_source[0] + "\n");
		
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
			};System.out.println(fs_source[0]);
		
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
				"	vec4 P = vpMatrix.viewMat * position;			\n" + 
				"													\n" + 
				"	vs_out.N = mat3(vpMatrix.viewMat) * normal;			\n" + 
				"	vs_out.L = mat3(vpMatrix.viewMat) * shading.light_pos - P.xyz;	\n" + 
				"	vs_out.V = -P.xyz;								\n" + 
				"	vs_out.shadowCoord = shading.shadowMat * position;	\n" + 
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
				//gathered into light uniform blocks
				//"layout (location = 11) uniform vec2 shadowMapSize;		\n" + 
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
				//"	float shadowed = textureProj(shadow_tex, fs_in.shadowCoord);	\n" + 
				//"	color = shadowed * vec4(ambient * 0.5 + diffuse, 1.0) + vec4(ambient * 0.5, 1.0);	\n" + 
				"														\n" + 
				"	color = PCF(fs_in.shadowCoord) * vec4(diffuse, 1.0) + vec4(ambient, 1.0);	\n" + 
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
	
	protected void setupVertices()
	{
		vertices[0] = new Vertex(-25.8f, 0f, -25.8f, 0);
		vertices[1] = new Vertex(-25.8f, 0f, 25.8f, 1);
		vertices[2] = new Vertex(25.8f, 0f, -25.8f, 2);
		vertices[3] = new Vertex(25.8f, 0f, 25.8f, 3);
		
		for (int n = 0; n < vertices.length; n++)
		{
			vertBuff.put(vertices[n].getPosition());
		}
		
		vertBuff.rewind();
	}
	
	protected void setupNormals()
	{
		for (int i = 0; i < vertices.length; i++)
		{
			vertices[i].setNormal(0f, 1f, 0f);
			
			normBuff.put(vertices[i].getNormal());
		}
		
		normBuff.rewind();
	}
	
	protected void setupTexCoords()
	{
		vertices[0].setTexCoords(new float[]{0f, 1f});
		vertices[1].setTexCoords(new float[]{0f, 0f});
		vertices[2].setTexCoords(new float[]{1f, 1f});
		vertices[3].setTexCoords(new float[]{1f, 0f});
		
		for (int n = 0; n < vertices.length; n++)
		{
			texBuff.put(vertices[n].getTexCoords());
		}
		
		texBuff.rewind();
	}
	
	protected void setupBuffers()
	{
		//generate vao
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
		gl.glBufferData(GL4.GL_ARRAY_BUFFER, vertBuffSize * Float.SIZE / 8, normBuff, GL4.GL_STATIC_DRAW);
		gl.glVertexAttribPointer(nor_inLoc, 3, GL4.GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(nor_inLoc);
		
		//bind the texCoord buffer
		gl.glBindBuffer(GL4.GL_ARRAY_BUFFER, vboBuff.get(2));
		gl.glBufferData(GL4.GL_ARRAY_BUFFER, texBuffSize * Float.SIZE / 8, texBuff, GL4.GL_STATIC_DRAW);
		gl.glVertexAttribPointer(tc_inLoc, 2, GL4.GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(tc_inLoc);
	}
	
	protected void setupTextures()
	{
		String fileDir = "images/soil.jpg";
		TextureData texData = null;
		
		try
		{
			texData = TextureIO.newTextureData(gl.getGLProfile(), 
					new File(fileDir), false, TextureIO.JPG);
			
			texDataBuff = texData.getBuffer();
			texDataBuff.rewind();
			
			//notice that the image used must be square
			imageSize = texData.getHeight();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		gl.glGenTextures(1, texObjBuff);
		gl.glBindTexture(GL4.GL_TEXTURE_2D, texObjBuff.get(0));
		
		gl.glTexStorage2D(GL4.GL_TEXTURE_2D, 0, GL4.GL_RGBA8, imageSize, imageSize);
		gl.glTexImage2D(GL4.GL_TEXTURE_2D, 0, GL4.GL_RGBA8, imageSize, imageSize, 
				0, texData.getPixelFormat(), texData.getPixelType(), texDataBuff);
		gl.glTexParameteri(GL4.GL_TEXTURE_2D, GL4.GL_TEXTURE_MAG_FILTER, GL4.GL_LINEAR);
		gl.glTexParameteri(GL4.GL_TEXTURE_2D, GL4.GL_TEXTURE_MIN_FILTER, GL4.GL_LINEAR);
	}
	
	protected void loadTexture()
	{
		try
		{
			BufferedImage image = ImageIO.read(new File("images/soil.jpg"));
			texture = AWTTextureIO.newTexture(GLProfile.getDefault(), image, true);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public void renderDepth()
	{
		/*gl.glUseProgram(depthProgram);
		
		gl.glBindVertexArray(vaoBuff.get(0));
		gl.glDrawArrays(GL4.GL_TRIANGLE_STRIP, 0, 4);*/
	}
	
	public void renderView()
	{
		gl.glUseProgram(viewProgram);
		
		//this method will not be remembered by vao
		gl.glActiveTexture(GL4.GL_TEXTURE0);
		texture.bind(gl);
		//gl.glBindTexture(GL4.GL_TEXTURE_2D, texObjBuff.get(0));
		
		gl.glBindVertexArray(vaoBuff.get(0));
		gl.glDrawArrays(GL4.GL_TRIANGLE_STRIP, 0, 4);
	}
	
	public void destroy()
	{
		
	}
}
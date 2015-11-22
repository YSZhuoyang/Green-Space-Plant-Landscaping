package Support;

import StandardObjects.Object3D;

import static Support.Resources.*;

import java.io.File;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.IntBuffer;

import com.jogamp.opengl.GL4;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.texture.TextureIO;
import com.jogamp.opengl.util.texture.TextureData;


/**
 * @author Yu Sangzhuoyang
 * @version 4.19
 */
public class Skybox extends Object3D
{
	private int imageSize;
	
	private IntBuffer cubeTexBuff;
	private Buffer[] texDataBuff;
	
	public Skybox()
	{
		vaoBuff = GLBuffers.newDirectIntBuffer(1);
		cubeTexBuff = GLBuffers.newDirectIntBuffer(1);
		
		texDataBuff = new Buffer[6];
	}
	
	public void initialize()
	{
		initViewProgram();
		setupTexture();
	}
	
	protected void initViewProgram()
	{
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
				//"layout (location = 3) uniform mat4 viewMatrix;		\n" + 
				//notice that the texCoord is used with the form of vec3
				"out vec3 tc;										\n" + 
				"void main(void)									\n" + 
				"{													\n" + 
				"	vec3[4] vertices = vec3[4](vec3(-1.0, -1.0, 1.0),	\n" + 
				"								vec3(1.0, -1.0, 1.0),	\n" + 
				"								vec3(-1.0, 1.0, 1.0),	\n" + 
				"								vec3(1.0,  1.0, 1.0));	\n" + 
				"													\n" + 
				"	tc = mat3(vpMatrix.viewMat) * vertices[gl_VertexID];	\n" + 
				"													\n" + 
				"	gl_Position = vec4(vertices[gl_VertexID], 1.0);	\n" + 
				"}"
			};//System.out.println("\n" + "branch: \n" + vs_source_bran[0] + "\n");
		
		gl.glShaderSource(verShader_bran, 1, vs_source_bran, null);
		gl.glCompileShader(verShader_bran);
		InfoLog.printShaderInfoLog(verShader_bran);
		
		String[] fs_source_bran = 
			{
				// Output to the framebuffer
				"#version 430										\n" + 
				"layout (binding = 0) uniform samplerCube tex_object;	\n" + 
				"in vec3 tc;										\n" + 
				"out vec4 color;									\n" + 
				"void main(void)									\n" + 
				"{													\n" + 
				"	color = texture(tex_object, tc);				\n" + 
				//"	color = vec4(1, 1, 1, 1.0);						\n" + 
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
	
	protected void setupVertices() {}
	
	protected void setupBuffers()
	{
		//generate vao
		gl.glGenVertexArrays(1, vaoBuff);
		gl.glBindVertexArray(vaoBuff.get(0));
	}
	
	protected void setupTexture()
	{
		String fileDir;
		TextureData texData = null;
		
		for (int i = 0; i < 6; i++)
		{
			fileDir = "images/skybox/sky" + (i + 1) + ".jpg";
			
			try
			{
				texData = TextureIO.newTextureData(gl.getGLProfile(), 
						new File(fileDir), false, TextureIO.JPG);
				
				texDataBuff[i] = texData.getBuffer();
				texDataBuff[i].rewind();
				
				//notice that the image used for skybox must be square
				imageSize = texData.getHeight();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		
		gl.glGenTextures(1, cubeTexBuff);
		gl.glBindTexture(GL4.GL_TEXTURE_CUBE_MAP, cubeTexBuff.get(0));
		
		gl.glTexStorage2D(GL4.GL_TEXTURE_CUBE_MAP, 0, GL4.GL_RGB8, imageSize, imageSize);
		
		for (int i = 0; i < 6; i++)
		{
			gl.glTexImage2D(GL4.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, GL4.GL_RGB8, 
					imageSize, imageSize, 0, texData.getPixelFormat(), texData.getPixelType(), texDataBuff[i]);
		}
		
		gl.glTexParameteri(GL4.GL_TEXTURE_CUBE_MAP, GL4.GL_TEXTURE_MAG_FILTER, GL4.GL_LINEAR);
		gl.glTexParameteri(GL4.GL_TEXTURE_CUBE_MAP, GL4.GL_TEXTURE_MIN_FILTER, GL4.GL_LINEAR);
	}
	
	//notice that it must be rendered with depth test disabled
	public void renderView()
	{
		gl.glDisable(GL4.GL_DEPTH_TEST);
		gl.glEnable(GL4.GL_TEXTURE_CUBE_MAP_SEAMLESS);
		
		gl.glUseProgram(viewProgram);
		
		//Camera.setViewMatUniform();
		
		gl.glActiveTexture(GL4.GL_TEXTURE0);
		gl.glBindTexture(GL4.GL_TEXTURE_CUBE_MAP, cubeTexBuff.get(0));
		
		gl.glBindVertexArray(vaoBuff.get(0));
		gl.glDrawArrays(GL4.GL_TRIANGLE_STRIP, 0, 4);
		
		gl.glEnable(GL4.GL_DEPTH_TEST);
	}
	
	public void destroy()
	{
		
	}
}
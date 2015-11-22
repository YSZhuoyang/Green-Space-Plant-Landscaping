package Support;

import static Support.Resources.*;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.util.texture.awt.AWTTextureIO;


/**
 * @author Yu Sangzhuoyang
 * @version 4.19
 */
public class RainOld extends ParticlesysOld
{
	
	public RainOld(int pc)
	{
		super(pc);
		
		birthRate = 720;
		speed = 160f;
	}
	
	public void initParticle(int i)
	{
		particles[i].position.setCoords(((float) Math.random() - 0.5f) * 20f, 
				((float) Math.random() * 25) + 25f, 
				((float) Math.random() - 0.5f) * 20f);
		particles[i].velocity.setCoords(dir.dot(speed));
		particles[i].age = 0f;
		particles[i].lifespan = 1f;
		particles[i].size = 0.02f;
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
				"layout (std140, binding = 8) uniform BillMatBlock	\n" + 
				"{													\n" + 
				"	vec3 camUp_world;								\n" + 
				"	vec3 camPos_world;								\n" + 
				"} billVect;										\n" + 
				"													\n" + 
				"layout (location = 0) in vec4 pAnds;				\n" + 
				//"layout (location = 2) in vec2 tc_in;				\n" + 
				"out vec2 tc;										\n" + 
				"													\n" + 
				"void main(void)									\n" + 
				"{													\n" + 
				"	float scaleY = 30;								\n" + 
				"	vec2[4] vertices = vec2[4](vec2(-1.0, -1.0),	\n" + 
				"								vec2(1.0, -1.0),	\n" + 
				"								vec2(-1.0, 1.0),	\n" + 
				"								vec2(1.0,  1.0));	\n" + 
				"													\n" + 
				"	vec3 camDir_world = pAnds.xyz - billVect.camPos_world;	\n" + 
				"	vec3 camRight_world = normalize(cross(camDir_world, billVect.camUp_world));	\n" + 
				"													\n" + 
				//"	vec3 vertPos_world = pAnds.xyz + (camRight_world * vertices[gl_VertexID].x " + 
				//"+ camUp_world * vertices[gl_VertexID].y * scaleY) * pAnds.w;			\n" + 
				"													\n" + 
				"	vec3 vertPos_world = pAnds.xyz + (camRight_world * vertices[gl_VertexID].x " + 
				"+ billVect.camUp_world * vertices[gl_VertexID].y * scaleY) * pAnds.w;			\n" + 
				"													\n" + 
				"	gl_Position = vpMatrix.proMat * vpMatrix.viewMat * vec4(vertPos_world, 1.0);	\n" + 
				"													\n" + 
				//"	gl_Position = proMatrix * viewMatrix * vec4((vertices[gl_VertexID] * pAnds.w + pAnds.xyz), 1.0);	\n" + 
				"													\n" + 
				//"	tc = tc_in;										\n" + 
				"	vec2[4] texCoords = vec2[4](vec2(0.0, 0.0),		\n" + 
				"								vec2(1.0, 0.0),		\n" + 
				"								vec2(0.0, 1.0),		\n" + 
				"								vec2(1.0, 1.0));	\n" + 
				"													\n" + 
				"	tc = texCoords[gl_VertexID];					\n" + 
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
				"in vec2 tc;										\n" + 
				"out vec4 color;									\n" + 
				"													\n" + 
				"void main(void)									\n" + 
				"{													\n" + 
				"	vec4 baseColor = texture(tex_object, tc);		\n" + 
				"													\n" + 
				"	if (baseColor.a < 0.5)							\n" + 
				"		discard;									\n" + 
				"													\n" + 
				"	color = baseColor;								\n" + 
				//"	color = vec4(0.55, 0.55, 0.7, 1.0);				\n" + 
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
	
	protected void loadTexture()
	{
		try
		{
			BufferedImage image = ImageIO.read(new File("images/raindrop.png"));
			texture = AWTTextureIO.newTexture(GLProfile.getDefault(), image, true);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public void renderView()
	{
		animate();
		
		if (currentCount > 0)
		{
			gl.glUseProgram(viewProgram);
			
			gl.glActiveTexture(GL4.GL_TEXTURE0);
			texture.bind(gl);
			
			gl.glBindVertexArray(vaoBuff.get(0));
			gl.glBindBuffer(GL4.GL_ARRAY_BUFFER, vboBuff.get(0));
			gl.glBufferSubData(GL4.GL_ARRAY_BUFFER, 0, currentCount * 4 * Float.SIZE / 8, vertBuff);
			
			gl.glDrawArraysInstanced(GL4.GL_TRIANGLE_STRIP, 0, 4, currentCount);
		}
	}
}
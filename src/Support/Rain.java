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
public class Rain extends Particlesys
{
	public Rain(int pc)
	{
		super(pc);
		
		birthRate = 1980;
		speed = 150f;
		
		turnOff();
	}
	
	protected void initCompProgram()
	{
		int compShader = gl.glCreateShader(GL4.GL_COMPUTE_SHADER);
		
		String[] cs_source = 
			{
				"#version 430										\n" + 
				//"layout uniform float timestep = 1.0 / 60.0;		\n" + 
				"layout (location = 1) uniform int newParticles;	\n" + 
				"layout (binding = 4) uniform atomic_uint counter;	\n" + 
				"layout (local_size_x = 100) in;					\n" + 
				"													\n" + 
				"layout (std430, binding = 0) buffer pAnds_in		\n" + 
				"{													\n" + 
				"	vec4 pAnds[];									\n" + 
				"} input_pAnds;										\n" + 
				"													\n" + 
				"layout (std430, binding = 1) buffer velocity_in	\n" + 
				"{													\n" + 
				//must use vec4 instead of vec3 to avoid memory allocation 
				//problem caused by std430 qualifier
				"	vec4 velocity[];								\n" + 
				"} input_vel;										\n" + 
				"													\n" + 
				"layout (std430, binding = 2) buffer lifespan_in	\n" + 
				"{													\n" + 
				"	float lifespan[];								\n" + 
				"} input_ls;										\n" + 
				"													\n" + 
				"layout (std430, binding = 3) buffer age_in			\n" + 
				"{													\n" + 
				"	float age[];									\n" + 
				"} input_age;										\n" + 
				"													\n" + 
				"void main()										\n" + 
				"{													\n" + 
				//no idea if it's correct when both groupNumX and groupNumY is not 1
				"	int id = int(gl_GlobalInvocationID.x);			\n" + 
				"													\n" + 
				"	float timestep = 1.0 / 60.0;					\n" + 
				"													\n" + 
				"	if (input_ls.lifespan[id] > 0)					\n" + 
				"	{												\n" + 
				"		input_age.age[id] += timestep;				\n" + 
				"													\n" + 
				"		if (input_age.age[id] > input_ls.lifespan[id])	\n" + 
				"		{											\n" + 
				//reset and initialize data
				"			input_ls.lifespan[id] = 0.0;			\n" + 
				"			input_age.age[id] = 0.0;				\n" + 
				"			input_pAnds.pAnds[id].y = 50;			\n" + 
				"		}											\n" + 
				"		else										\n" + 
				"		{											\n" + 
				"			vec3 dis = input_vel.velocity[id].xyz * timestep;	\n" + 
				"													\n" + 
				"			input_pAnds.pAnds[id].xyz += dis;		\n" + 
				"		}											\n" + 
				"	}												\n" + 
				"	else											\n" + 
				"	{												\n" + 
				//location need to be modified
				"		uint count = atomicCounterIncrement(counter);	\n" + 
				"													\n" + 
				"		if (count <= newParticles)					\n" + 
				"		{											\n" + 
				"			input_ls.lifespan[id] = 1.0;			\n" + 
				"		}											\n" + 
				"	}												\n" + 
				"}"
			};
		
		gl.glShaderSource(compShader, 1, cs_source, null);
		gl.glCompileShader(compShader);
		InfoLog.printShaderInfoLog(compShader);
		
		computeProgram = gl.glCreateProgram();
		gl.glAttachShader(computeProgram, compShader);
		gl.glLinkProgram(computeProgram);
		gl.glValidateProgram(computeProgram);
		InfoLog.printProgramInfoLog(computeProgram);
		
		gl.glDeleteShader(compShader);
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
				"layout (location = 2) in float lifespan;			\n" + 
				"out vec2 tc;										\n" + 
				"out float out_lifespan;							\n" + 
				"													\n" + 
				"void main(void)									\n" + 
				"{													\n" + 
				"	float scaleY = 40;								\n" + 
				"	vec2[4] vertices = vec2[4](vec2(-1.0, -1.0),	\n" + 
				"								vec2(1.0, -1.0),	\n" + 
				"								vec2(-1.0, 1.0),	\n" + 
				"								vec2(1.0,  1.0));	\n" + 
				"													\n" + 
				"	vec3 camDir_world = pAnds.xyz - billVect.camPos_world;							\n" + 
				"	vec3 camRight_world = normalize(cross(camDir_world, billVect.camUp_world));		\n" + 
				"													\n" + 
				"	vec3 vertPos_world = pAnds.xyz + (camRight_world * vertices[gl_VertexID].x " + 
				"+ billVect.camUp_world * vertices[gl_VertexID].y * scaleY) * pAnds.w;				\n" + 
				"													\n" + 
				"	gl_Position = vpMatrix.proMat * vpMatrix.viewMat * vec4(vertPos_world, 1.0);	\n" + 
				"													\n" + 
				"	vec2[4] texCoords = vec2[4](vec2(0.0, 0.0),		\n" + 
				"								vec2(1.0, 0.0),		\n" + 
				"								vec2(0.0, 1.0),		\n" + 
				"								vec2(1.0, 1.0));	\n" + 
				"													\n" + 
				"	tc = texCoords[gl_VertexID];					\n" + 
				"													\n" + 
				"	out_lifespan = lifespan;						\n" + 
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
				"in float out_lifespan;								\n" + 
				"out vec4 color;									\n" + 
				"													\n" + 
				"void main(void)									\n" + 
				"{													\n" + 
				"	vec4 baseColor = texture(tex_object, tc);		\n" + 
				"													\n" + 
				"	if (out_lifespan == 0 || baseColor.a < 0.5)		\n" + 
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
	
	protected void initParticles()
	{
		float[] velocity = dir.dot(speed);
		
		for (int i = 0; i < particleCount; i++)
		{
			pAndsBuff.put(new float[] {((float) Math.random() - 0.5f) * 50f, 
					50f, 
					((float) Math.random() - 0.5f) * 50f, 
					0.016f});
			velBuff.put(new float[] {velocity[0], velocity[1], velocity[2], 0f});
			ageBuff.put(0f);
			
			if (i < startCount)
			{
				lifespanBuff.put(1f);
			}
			else
			{
				lifespanBuff.put(0f);
			}
		}
		
		pAndsBuff.rewind();
		velBuff.rewind();
		lifespanBuff.rewind();
		ageBuff.rewind();
		
		newParticles = (int) (birthRate * timeBetweenAni);
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
}
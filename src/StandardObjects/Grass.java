package StandardObjects;

import Support.InfoLog;
import Support.Vertex;

import static Support.Resources.*;

import com.jogamp.opengl.GL4;
import com.jogamp.opengl.util.GLBuffers;


/**
 * @author Yu Sangzhuoyang
 * @version 5.10
 */
public class Grass extends Object3D
{
	private int verPosLoc;
	private int nor_inLoc;
	
	public Grass()
	{
		vertices = new Vertex[5];
		
		vertBuffSize = vertices.length * 3;
		
		vboCount = 2;
		vaoBuff = GLBuffers.newDirectIntBuffer(1);
		vboBuff = GLBuffers.newDirectIntBuffer(vboCount);
		vertBuff = GLBuffers.newDirectFloatBuffer(vertBuffSize);
		normBuff = GLBuffers.newDirectFloatBuffer(vertBuffSize);
		
		verPosLoc = 0;
		nor_inLoc = 1;
	}
	
	public void initialize()
	{
		initDepthProgram();
		initViewProgram();
		setupVertices();
		setupNormals();
		setupBuffers();
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
				"layout (location = 0) in vec4 vVertex;				\n" + 
				"													\n" + 
				"int pseudo_random(int seed, int iterations)		\n" + 
				"{													\n" + 
				"	int value = seed;								\n" + 
				"	int n;											\n" + 
				"	for (n = 0; n < iterations; n++)				\n" + 
				"	{												\n" + 
				"		value = ((value >> 7) ^ (value << 9)) * 15469548;	\n" + 
				"	}												\n" + 
				"	return value;									\n" + 
				"}													\n" + 
				"													\n" + 
				"mat4 randomly_rotateY(float angle)					\n" + 
				"{													\n" + 
				"	float st = sin(angle);							\n" + 
				"	float ct = cos(angle);							\n" + 
				"													\n" + 
				"	return mat4(vec4(ct, 0, st, 0),					\n" + 
				"				vec4(0, 1, 0, 0),					\n" + 
				"				vec4(-st, 0, ct, 0),				\n" + 
				"				vec4(0, 0, 0, 1));					\n" + 
				"}													\n" + 
				"													\n" + 
				"mat4 randomly_rotateX(float angle)					\n" + 
				"{													\n" + 
				"	float st = sin(angle);							\n" + 
				"	float ct = cos(angle);							\n" + 
				"													\n" + 
				"	return mat4(vec4(1, 0, 0, 0),					\n" + 
				"				vec4(0, ct, -st, 0),				\n" + 
				"				vec4(0, st, ct, 0),					\n" + 
				"				vec4(0, 0, 0, 1));					\n" + 
				"}													\n" + 
				"													\n" + 
				"void main(void)									\n" + 
				"{													\n" + 
				//"	vec4 offset = vec4((float(gl_InstanceID >> 9) - 256) / 10, 0f, 	\n" + 
				//"(float(gl_InstanceID & 0x1FF) - 256) / 10, 0f);					\n" + 
				"																	\n" + 
				"	vec4 offset = vec4((float(gl_InstanceID >> 10) - 512) / 20, 0f, \n" + 
				"(float(gl_InstanceID & 0x3FF) - 512) / 20, 0f);					\n" + 
				"																	\n" + 
				"	int numA = pseudo_random(gl_InstanceID, 3) & 0xFF - 128;		\n" + 
				"	float numC = float(numA) / 750;									\n" + 
				"	offset += vec4(float(numA) / 500, 0f, float(numA) / 500, 0f);	\n" + 
				"																	\n" + 
				"	vec4 position = randomly_rotateY(numC * 20) * vVertex;			\n" + 
				"	position = randomly_rotateX(numC * 9) * position + offset;		\n" + 
				"	position *= vec4(1, 0.7 + numC * 4, 1, 1);						\n" + 
				"																	\n" + 
				"	gl_Position = depthTrans.lightMat * position;					\n" + 
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
				"layout (location = 0) in vec4 vVertex;				\n" + 
				"layout (location = 1) in vec3 normal;				\n" + 
				"out vec4 color;									\n" + 
				"													\n" + 
				"out VS_OUT											\n" + 
				"{													\n" + 
				"	vec3 N;											\n" + 
				"	vec3 L;											\n" + 
				"	vec3 V;											\n" + 
				"	vec4 shadowCoord;								\n" + 
				"} vs_out;											\n" + 
				"													\n" + 
				"int pseudo_random(int seed, int iterations)		\n" + 
				"{													\n" + 
				"	int value = seed;								\n" + 
				"	int n;											\n" + 
				"	for (n = 0; n < iterations; n++)				\n" + 
				"	{												\n" + 
				"		value = ((value >> 7) ^ (value << 9)) * 15469548;	\n" + 
				"	}												\n" + 
				"	return value;									\n" + 
				"}													\n" + 
				"													\n" + 
				"mat4 randomly_rotateY(float angle)					\n" + 
				"{													\n" + 
				"	float st = sin(angle);							\n" + 
				"	float ct = cos(angle);							\n" + 
				"													\n" + 
				"	return mat4(vec4(ct, 0, st, 0),					\n" + 
				"				vec4(0, 1, 0, 0),					\n" + 
				"				vec4(-st, 0, ct, 0),				\n" + 
				"				vec4(0, 0, 0, 1));					\n" + 
				"}													\n" + 
				"													\n" + 
				"mat4 randomly_rotateX(float angle)					\n" + 
				"{													\n" + 
				"	float st = sin(angle);							\n" + 
				"	float ct = cos(angle);							\n" + 
				"													\n" + 
				"	return mat4(vec4(1, 0, 0, 0),					\n" + 
				"				vec4(0, ct, -st, 0),				\n" + 
				"				vec4(0, st, ct, 0),					\n" + 
				"				vec4(0, 0, 0, 1));					\n" + 
				"}													\n" + 
				"													\n" + 
				"void main(void)									\n" + 
				"{													\n" + 
				//"	vec4 offset = vec4((float(gl_InstanceID >> 9) - 256) / 10, 0f, 	\n" + 
				//"(float(gl_InstanceID & 0x1FF) - 256) / 10, 0f);					\n" + 
				"																	\n" + 
				"	vec4 offset = vec4((float(gl_InstanceID >> 10) - 512) / 20, 0f, \n" + 
				"(float(gl_InstanceID & 0x3FF) - 512) / 20, 0f);					\n" + 
				"																	\n" + 
				"	int numA = pseudo_random(gl_InstanceID, 3) & 0xFF - 128;		\n" + 
				"	float numC = float(numA) / 750;									\n" + 
				"	offset += vec4(float(numA) / 500, 0f, float(numA) / 500, 0f);	\n" + 
				"																	\n" + 
				"	vec4 position = randomly_rotateY(numC * 10) * vVertex;			\n" + 
				"	position = randomly_rotateX(numC * 8) * position + offset;		\n" + 
				"	position *= vec4(1, 0.7 + numC * 4, 1, 1);						\n" + 
				"																	\n" + 
				"	vs_out.N = mat3(randomly_rotateY(numC * 20)) * normal;			\n" + 
				"	vs_out.N = mat3(randomly_rotateX(numC * 9)) * vs_out.N;			\n" + 
				"	vs_out.N = mat3(vpMatrix.viewMat) * vs_out.N;					\n" + 
				"																	\n" + 
				"	vec4 P = vpMatrix.viewMat * position;							\n" + 
				"	vs_out.V = -P.xyz;												\n" + 
				"	vs_out.L = mat3(vpMatrix.viewMat) * shading.light_pos - P.xyz;	\n" + 
				"																	\n" + 
				"	vs_out.shadowCoord = shading.shadowMat * position;				\n" + 
				"																	\n" + 
				"	color = vec4(0.15 + numC, 0.5 + numC, 0.06 + numC, 0);			\n" + 
				"																	\n" + 
				"	gl_Position = vpMatrix.proMat * P;								\n" + 
				"}"
			};//System.out.println("\n" + "grass: \n" + vs_source[0] + "\n");
		
		gl.glShaderSource(verShader, 1, vs_source, null);
		gl.glCompileShader(verShader);
		InfoLog.printShaderInfoLog(verShader);
		
		String[] fs_source = 
			{
				// Output to the framebuffer
				"#version 430										\n" + 
				"													\n" + 
				"layout (std140, binding = 9) uniform lightPosBlock	\n" + 
				"{													\n" + 
				"	mat4 shadowMat;									\n" + 
				"	vec3 light_pos;									\n" + 
				"	vec2 shadowMapSize;								\n" + 
				"} shading;											\n" + 
				"													\n" + 
				"layout (binding = 5) uniform sampler2DShadow shadow_tex;	\n" + 
				//"layout (location = 11) uniform vec2 shadowMapSize;	\n" + 
				"in vec4 color;										\n" + 
				"out vec4 output_color;								\n" + 
				"													\n" + 
				"in VS_OUT											\n" + 
				"{													\n" + 
				"	vec3 N;											\n" + 
				"	vec3 L;											\n" + 
				"	vec3 V;											\n" + 
				"	vec4 shadowCoord;								\n" + 
				"} fs_in;											\n" + 
				"													\n" + 
				"float PCF(vec4 centerCoord)						\n" + 
				"{													\n" + 
				"	float factor = 0.0;								\n" + 
				"	vec4 offsets;									\n" + 
				"	vec4 UVC;										\n" + 
				"													\n" + 
				"	for (int y = -1; y <= 1; y++)					\n" + 
				"	{												\n" + 
				"		for (int x = -1; x <= 1; x++)				\n" + 
				"		{											\n" + 
				"			offsets = vec4(x / shading.shadowMapSize.x * centerCoord.w, " + 
				"y / shading.shadowMapSize.y * centerCoord.w, 0.0, 0.0);	\n" + 
				"			UVC = vec4(centerCoord + offsets);		\n" + 
				"													\n" + 
				"			factor += textureProj(shadow_tex, UVC);	\n" + 
				"		}											\n" + 
				"	}												\n" + 
				"													\n" + 
				"	factor = 0.2 + factor / 11.25;					\n" + 
				"													\n" + 
				"	return factor;									\n" + 
				"}													\n" + 
				"													\n" + 
				"void main(void)									\n" + 
				"{													\n" + 
				/*"	vec3 ambient_albedo = color.rgb;				\n" + 
				"	vec3 diffuse_albedo = color.rgb;				\n" + 
				"													\n" + 
				"	vec3 N = normalize(fs_in.N);					\n" + 
				"	vec3 L = normalize(fs_in.L);					\n" + 
				"	vec3 V = normalize(fs_in.V);					\n" + 
				"													\n" + 
				"	vec3 ambient = 0.5 * ambient_albedo;			\n" + 
				"	vec3 diffuse = max(dot(N, L), 0.0) * diffuse_albedo;\n" + */
				"													\n" + 
				"	output_color = PCF(fs_in.shadowCoord) * color;	\n" + 
				"													\n" + 
				//"	float shadowed = textureProj(shadow_tex, fs_in.shadowCoord);	\n" + 
				"													\n" + 
				//"	output_color = shadowed * vec4(ambient * 0.8 + diffuse, 1.0) +  vec4(ambient * 0.2, 1.0);	\n" + 
				//"	output_color = shadowed * color * 0.8 + color * 0.2;	\n" + 
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
		vertices[0] = new Vertex(-0.005f, 0f, 0f, 0);
		vertices[1] = new Vertex(0.005f, 0f, 0f, 1);
		vertices[2] = new Vertex(-0.005f, 0.08f, 0f, 2);
		vertices[3] = new Vertex(0.005f, 0.08f, 0f, 3);
		vertices[4] = new Vertex(0f, 0.16f, 0f, 4);
		
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
			vertices[i].setNormal(0f, 0f, 1.0f);
			
			normBuff.put(vertices[i].getNormal());
		}
		
		normBuff.rewind();
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
	}
	
	public void renderDepth()
	{
		/*gl.glUseProgram(depthProgram);
		
		gl.glBindVertexArray(vaoBuff.get(0));
		gl.glDrawArraysInstanced(GL4.GL_TRIANGLE_STRIP, 0, 5, 1024 * 1024);*/
	}
	
	public void renderView()
	{
		gl.glUseProgram(viewProgram);
		
		gl.glBindVertexArray(vaoBuff.get(0));
		gl.glDrawArraysInstanced(GL4.GL_TRIANGLE_STRIP, 0, 5, 1024 * 1024);
	}
	
	public void destroy()
	{
		
	}
}
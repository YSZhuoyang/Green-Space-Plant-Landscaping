package StandardObjects;

import Support.InfoLog;
import Support.Transformation;

import static Support.Resources.*;

import java.util.ArrayDeque;
import java.util.Queue;
import java.io.File;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.texture.awt.AWTTextureIO;


/**
 * @author Yu Sangzhuoyang
 * @version 4.19
 */
public class LeafBlade extends Object3D
{
	private float width;
	private float length;
	
	private int[] stripCount;
	private int[] firstOfStrip;
	
	private int instanceCount;
	
	//private int verPosLoc;
	//private int tc_inLoc;
	//private int d_idLoc;
	//private int modelMatLoc;
	
	private Queue<FloatBuffer> vertBuffQueue;
	private Queue<FloatBuffer> texBuffQueue;
	//private Queue<IntBuffer> indirectCmdQueue;
	
	public LeafBlade()
	{
		instanceCount = 0;
		
		vertBuffSize = 4 * 4 * 3;
		texBuffSize = 4 * 4 * 2;
		
		modelMat = new float[16];
		
		vaoBuff = GLBuffers.newDirectIntBuffer(2);
		vboBuff = GLBuffers.newDirectIntBuffer(2);
		texBuff = GLBuffers.newDirectFloatBuffer(texBuffSize);
		
		vertBuffQueue = new ArrayDeque<FloatBuffer>();
		texBuffQueue = new ArrayDeque<FloatBuffer>();
		//indirectCmdQueue = new ArrayDeque<IntBuffer>();
		
		verPosLoc = 0;
		tc_inLoc = 1;
		modelMatLoc = 5;
	}
	
	public void addLeaf(Transformation trans, float w, float l)
	{
		width = w;
		length = l;
		
		transMove = trans.clone();
		
		setupVertices();
		setupTexCoords();
	}
	
	public void generate()
	{
		initViewProgram();
		loadTexture();
		setupBuffers();
	}
	
	public void setModelMatrix(float[] m)
	{
		modelMat = m;
	}
	
	protected void initViewProgram()
	{
		//create shaders for rendering leaf blade
		int verShader = gl.glCreateShader(GL4.GL_VERTEX_SHADER);
		int tesConShader = gl.glCreateShader(GL4.GL_TESS_CONTROL_SHADER);
		int tesEvaShader = gl.glCreateShader(GL4.GL_TESS_EVALUATION_SHADER);
		int fraShader = gl.glCreateShader(GL4.GL_FRAGMENT_SHADER);
		
		//Tessellation control shader can be ignored
		String[] vs_source = 
			{
				"#version 430 core									\n" + 
				"													\n" + 
				"layout (std140, binding = 7) uniform VPMatBlock	\n" + 
				"{													\n" + 
				"	mat4 viewMat;									\n" + 
				"	mat4 proMat;									\n" + 
				"} vpMatrix;										\n" + 
				"													\n" + 
				"layout (location = 5) uniform mat4 modelMat;	\n" + 
				"layout (location = 0) in vec4 position;		\n" + 
				"layout (location = 1) in vec2 tc;				\n" + 
				//"layout (location = 10) in uint draw_id;		\n" + 
				"out vec2 tc_out;								\n" + 
				"void main(void)								\n" + 
				"{												\n" + 
				"	tc_out = tc;								\n" + 
				"												\n" + 
				"	gl_Position = vpMatrix.viewMat * modelMat * position;\n" + 
                "}												\n"
			};
		
		gl.glShaderSource(verShader, 1, vs_source, null);
		gl.glCompileShader(verShader);
		InfoLog.printShaderInfoLog(verShader);
		
		String[] tcs_source = 
			{
				"#version 430 core							\n" + 
				"layout (vertices = 16) out;				\n" + 
				"in vec2 tc_out[];							\n" + 
				"out vec2 tc[];								\n" + 
				"void main(void)							\n" + 
				"{											\n" + 
				"	if (gl_InvocationID == 0)				\n" + 
				"	{										\n" + 
				"		gl_TessLevelInner[0] = 16.0;		\n" + 
				"		gl_TessLevelInner[1] = 16.0;		\n" + 
				"		gl_TessLevelOuter[0] = 16.0;		\n" + 
				"		gl_TessLevelOuter[1] = 16.0;		\n" + 
				"		gl_TessLevelOuter[2] = 16.0;		\n" + 
				"		gl_TessLevelOuter[3] = 16.0;		\n" + 
				"	}										\n" + 
				"											\n" + 
				"	tc[gl_InvocationID] = tc_out[gl_InvocationID];	\n" + 
				"											\n" + 
				"	gl_out[gl_InvocationID].gl_Position = gl_in[gl_InvocationID].gl_Position;\n" + 
                "}											\n"
			};
		
		gl.glShaderSource(tesConShader, 1, tcs_source, null);
		gl.glCompileShader(tesConShader);
		InfoLog.printShaderInfoLog(tesConShader);
		
		String[] tes_source = 
			{
				"#version 430 core									\n" + 
				"layout (quads, equal_spacing, cw) in;				\n" + 
				"layout (location = 7) uniform vec3 light_pos;		\n" + 
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
				"													\n" + 
				"in vec2 tc[];										\n" + 
				"out vec2 tc_out;									\n" + 
				"													\n" + 
				"out VS_OUT											\n" + 
				"{													\n" + 
				"	vec3 N;											\n" + 
				"	vec3 L;											\n" + 
				"	vec3 V;											\n" + 
				//"	vec4 shadowCoord;								\n" + 
				"} vs_out;											\n" + 
				"													\n" + 
				"vec4 quad_bezier(vec4 A, vec4 B, vec4 C, float t)	\n" + 
				"{													\n" + 
				"	vec4 D = mix(A, B, t);							\n" + 
				"	vec4 E = mix(B, C, t);							\n" + 
				"													\n" + 
				"	return mix(D, E, t);							\n" + 
				"}													\n" + 
				"													\n" + 
				"vec4 cubic_bezier(vec4 A, vec4 B, vec4 C, vec4 D, float t)	\n" + 
				"{													\n" + 
				"	vec4 E = mix(A, B, t);							\n" + 
				"	vec4 F = mix(B, C, t);							\n" + 
				"	vec4 G = mix(C, D, t);							\n" + 
				"													\n" + 
				"	return quad_bezier(E, F, G, t);					\n" + 
				"}													\n" + 
				"													\n" + 
				"vec4 evaluate_patch(vec2 at)						\n" + 
				"{													\n" + 
				"	vec4 P[4];										\n" + 
				"	int i;											\n" + 
				"													\n" + 
				"	for (i = 0; i < 4; i++)							\n" + 
				"	{												\n" + 
				"		P[i] = cubic_bezier(gl_in[i + 0].gl_Position, 	\n" + 
				"							gl_in[i + 4].gl_Position, 	\n" + 
				"							gl_in[i + 8].gl_Position, 	\n" + 
				"							gl_in[i + 12].gl_Position, 	\n" + 
				"							at.y);					\n" + 
				"	}												\n" + 
				"													\n" + 
				"	return cubic_bezier(P[0], P[1], P[2], P[3], at.x);	\n" + 
				"}													\n" + 
				"													\n" + 
				//used to create another two points forming three to 
				//calculate the normal vector
				"float epsilon = 0.001;								\n" + 
				"													\n" + 
				"void main(void)									\n" + 
				"{													\n" + 
				"	vec4 p1 = evaluate_patch(gl_TessCoord.xy);		\n" + 
				"	vec4 p2 = evaluate_patch(gl_TessCoord.xy + vec2(0.0, epsilon));	\n" + 
				"	vec4 p3 = evaluate_patch(gl_TessCoord.xy + vec2(epsilon, 0.0));	\n" + 
				"													\n" + 
				"	vec3 v1 = normalize(p2.xyz - p1.xyz);			\n" + 
				"	vec3 v2 = normalize(p3.xyz - p2.xyz);			\n" + 
				"													\n" + 
				"	vec2 tc1 = mix(tc[0], tc[3], gl_TessCoord.x);	\n" + 
				"	vec2 tc2 = mix(tc[12], tc[15], gl_TessCoord.x);	\n" + 
				"	vec2 tc3 = mix(tc2, tc1, gl_TessCoord.y);		\n" + 
				"													\n" + 
				"	tc_out = tc3;									\n" + 
				"													\n" + 
				"	vs_out.N = normalize(cross(v1, v2));			\n" + 
				"	vs_out.V = -p1.xyz;								\n" + 
				"	vs_out.L = mat3(vpMatrix.viewMat) * shading.light_pos - p1.xyz;	\n" + 
				"													\n" + 
				"	gl_Position = vpMatrix.proMat * p1;				\n" + 
                "}"
			};
		
		gl.glShaderSource(tesEvaShader, 1, tes_source, null);
		gl.glCompileShader(tesEvaShader);
		InfoLog.printShaderInfoLog(tesEvaShader);
		
		String[] fs_source = 
			{
				"#version 430 core									\n" + 
				"layout (binding = 0) uniform sampler2D tex_object;	\n" + 
				"out vec4 color;									\n" + 
				"in vec2 tc_out;									\n" + 
				//"in vec3 normal;									\n" + 
				"													\n" + 
				"in VS_OUT											\n" + 
				"{													\n" + 
				"	vec3 N;											\n" + 
				"	vec3 L;											\n" + 
				"	vec3 V;											\n" + 
				//"	vec4 shadowCoord;								\n" + 
				"} fs_in;											\n" + 
				"													\n" + 
				"void main(void)									\n" + 
				"{													\n" + 
				"	vec3 ambient_albedo = texture(tex_object, tc_out).rgb;	\n" + 
				"	vec3 diffuse_albedo = texture(tex_object, tc_out).rgb;	\n" + 
				"													\n" + 
				"	vec3 N = normalize(fs_in.N);					\n" + 
				"	vec3 L = normalize(fs_in.L);					\n" + 
				"	vec3 V = normalize(fs_in.V);					\n" + 
				"													\n" + 
				"	vec3 ambient = 0.5 * ambient_albedo;				\n" + 
				"	vec3 diffuse = max(dot(N, L), 0.0) * diffuse_albedo;\n" + 
				"													\n" + 
				"	color = vec4(ambient + diffuse, 1.0);			\n" + 
                "}"
			};
		
		gl.glShaderSource(fraShader, 1, fs_source, null);
		gl.glCompileShader(fraShader);
		InfoLog.printShaderInfoLog(fraShader);
		
		viewProgram = gl.glCreateProgram();
		gl.glAttachShader(viewProgram, verShader);
		gl.glAttachShader(viewProgram, tesConShader);
		gl.glAttachShader(viewProgram, tesEvaShader);
		gl.glAttachShader(viewProgram, fraShader);
		gl.glLinkProgram(viewProgram);
		gl.glValidateProgram(viewProgram);
		InfoLog.printProgramInfoLog(viewProgram);
		
		gl.glDeleteShader(verShader);
		gl.glDeleteShader(tesConShader);
		gl.glDeleteShader(tesEvaShader);
		gl.glDeleteShader(fraShader);
	}
	
	protected void setupVertices()
	{
		//setup vertices for rendering leaf blade
		vertBuff = GLBuffers.newDirectFloatBuffer(vertBuffSize);
		
		float[] ctrlPoints = {
				0f, 0f, 0f, 
				0f, 0f, 0f, 
				0f, 0f, 0f, 
				0f, 0f, 0f, 
				
				-width / 2, length * 0.3f, 0f, 
				-width / 4, length * 0.3f, width / 4, 
				width / 4, length * 0.3f, width / 4, 
				width / 2, length * 0.3f, 0f, 
				
				-width / 2, length * 0.7f, 0f, 
				-width / 4, length * 0.7f, width / 4, 
				width / 4, length * 0.7f, width / 4, 
				width / 2, length * 0.7f, 0f, 
				
				0f, length, 0f, 
				0f, length, 0f, 
				0f, length, 0f, 
				0f, length, 0f
				};
		
		transMove.applyTransToAll(ctrlPoints, ctrlPoints);
		
		vertBuff.put(ctrlPoints);
		vertBuff.rewind();
		vertBuffQueue.add(vertBuff);
		
		instanceCount++;
	}
	
	protected void setupTexCoords()
	{
		//Set leaf texture.
		float[] texCoords = {
				0f, 0f, 
				0.33f, 0f, 
				0.66f, 0f, 
				1f, 0f, 
				
				0f, 0.33f, 
				0.33f, 0.33f, 
				0.66f, 0.33f, 
				1f, 0.33f, 
				
				0f, 0.66f, 
				0.33f, 0.66f, 
				0.66f, 0.66f, 
				1f, 0.66f, 
				
				0f, 1f, 
				0.33f, 1f, 
				0.66f, 1f, 
				1f, 1f
				};
		
		texBuff = GLBuffers.newDirectFloatBuffer(texCoords);
		texBuffQueue.add(texBuff);
	}
	
	protected void setupBuffers()
	{
		gl.glGenVertexArrays(2, vaoBuff);
		
		//setup buffers for drawing leaf blade
		gl.glBindVertexArray(vaoBuff.get(0));
		
		stripCount = new int[instanceCount];
		firstOfStrip = new int[instanceCount];
		
		for (int i = 0; i < instanceCount; i++)
		{
			stripCount[i] = vertBuffSize / 3;
			firstOfStrip[i] = i * vertBuffSize / 3;
		}
		
		gl.glGenBuffers(2, vboBuff);
		
		gl.glBindBuffer(GL4.GL_ARRAY_BUFFER, vboBuff.get(0));
		//notice that buffer stores data in bits, thus the size of float (32bits, 
		//that is 4 bytes per float value) should be taken into consideration
		gl.glBufferData(GL4.GL_ARRAY_BUFFER, instanceCount * vertBuffSize * Float.SIZE / 8, null, GL4.GL_STATIC_DRAW);
		
		int count = 0;
		
		for (FloatBuffer vert : vertBuffQueue)
		{
			gl.glBufferSubData(GL4.GL_ARRAY_BUFFER, count * vertBuffSize * Float.SIZE / 8, vertBuffSize * Float.SIZE / 8, vert);
			count++;
		}
		
		gl.glVertexAttribPointer(verPosLoc, 3, GL4.GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(verPosLoc);
		gl.glPatchParameteri(GL4.GL_PATCH_VERTICES, 16);
		
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
	}
	
	protected void loadTexture()
	{
		try
		{
			BufferedImage image = ImageIO.read(new File("images/leaf.jpg"));
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
			
			//gl.glMultiDrawArraysIndirect(GL4.GL_PATCHES, 0, instanceCount, 0);
			gl.glBindVertexArray(vaoBuff.get(0));
			gl.glMultiDrawArrays(GL4.GL_PATCHES, firstOfStrip, 0, stripCount, 0, instanceCount);
		}
	}
	
	public void destroy()
	{
		
	}
}
package Support;

import static Support.Resources.*;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import com.jogamp.opengl.GL4;

public class InfoLog
{
	
	public InfoLog() {}
	
	public static void printShaderInfoLog(int shader)
	{
		IntBuffer intBuffer = IntBuffer.allocate(1);
		gl.glGetShaderiv(shader, GL4.GL_INFO_LOG_LENGTH, intBuffer);
		
		if (intBuffer.get(0) > 12)
		{
			int size = intBuffer.get(0);
			
			System.err.println("Shader compiling error: ");
			ByteBuffer byteBuffer = ByteBuffer.allocate(size);
			gl.glGetShaderInfoLog(shader, size, intBuffer, byteBuffer);
			
			for (byte b : byteBuffer.array())
			{
				System.err.print((char) b);
			}
			
			System.exit(1);
		}
	}
	
	public static void printProgramInfoLog(int program)
	{
		IntBuffer intBuffer = IntBuffer.allocate(1);
		gl.glGetProgramiv(program, GL4.GL_INFO_LOG_LENGTH, intBuffer);
		
		if (intBuffer.get(0) > 1)
		{
			int size = intBuffer.get(0);
			
			System.err.println("Shader compiling error: ");
			ByteBuffer byteBuffer = ByteBuffer.allocate(size);
			gl.glGetProgramInfoLog(program, size, intBuffer, byteBuffer);
			
			for (byte b : byteBuffer.array())
			{
				System.err.print((char) b);
			}
			
			System.exit(1);
		}
	}
}
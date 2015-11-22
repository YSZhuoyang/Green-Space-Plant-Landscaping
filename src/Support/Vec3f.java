package Support;


/**
 * @author Yu Sangzhuoyang
 * @version 3.30
 */
public class Vec3f
{
	private float[] coords;
	
	public Vec3f()
	{
		coords = new float[3];
	}
	
	public Vec3f(float[] c)
	{
		coords = new float[3];
		
		coords[0] = c[0];
		coords[1] = c[1];
		coords[2] = c[2];
	}
	
	public Vec3f(float x, float y, float z)
	{
		coords = new float[3];
		
		coords[0] = x;
		coords[1] = y;
		coords[2] = z;
	}
	
	public void normalize()
	{
		float length = (float) Math.sqrt(coords[0] * coords[0] + coords[1] * coords[1] + coords[2] * coords[2]);
		
		setX(coords[0] / length);
		setY(coords[1] / length);
		setZ(coords[2] / length);
	}
	
	public float[] plus(float[] x)
	{
		float[] res = new float[3];
		
		res[0] = coords[0] + x[0];
		res[1] = coords[1] + x[1];
		res[2] = coords[2] + x[2];
		
		return res;
	}
	
	public float[] dot(float x)
	{
		float[] res = new float[3];
		
		res[0] = coords[0] * x;
		res[1] = coords[1] * x;
		res[2] = coords[2] * x;
		
		return res;
	}
	
	public void add(float[] x)
	{
		coords[0] += x[0];
		coords[1] += x[1];
		coords[2] += x[2];
	}
	
	public void mult(float[] x)
	{
		coords[0] *= x[0];
		coords[1] *= x[1];
		coords[2] *= x[2];
	}
	
	public void setCoords(float[] c)
	{
		coords[0] = c[0];
		coords[1] = c[1];
		coords[2] = c[2];
	}
	
	public void setCoords(float x, float y, float z)
	{
		coords[0] = x;
		coords[1] = y;
		coords[2] = z;
	}
	
	public void setX(float x)
	{
		coords[0] = x;
	}
	
	public void setY(float y)
	{
		coords[1] = y;
	}
	
	public void setZ(float z)
	{
		coords[2] = z;
	}
	
	public float[] getCoords()
	{
		return coords;
	}
	
	public float getX()
	{
		return coords[0];
	}
	
	public float getY()
	{
		return coords[1];
	}
	
	public float getZ()
	{
		return coords[2];
	}
	
	public static void normalize(float[] v)
	{
		float length = (float) Math.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2]);
		
		v[0] /= length;
		v[1] /= length;
		v[2] /= length;
	}
	
	public static void crossProduct(float a[], float b[], float res[])
	{
		res[0] = a[1] * b[2] - b[1] * a[2];
		res[1] = a[2] * b[0] - b[2] * a[0];
		res[2] = a[0] * b[1] - b[0] * a[1];
	}
	
	public static void dot(float[] v, float x, float[] res)
	{
		res[0] = v[0] * x;
		res[1] = v[1] * x;
		res[2] = v[2] * x;
	}
}
package Support;


/**
 * @author Yu Sangzhuoyang
 * @version 3.15
 */
public class Vec2f
{
	private float[] coords;
	
	public Vec2f()
	{
		coords = new float[2];
	}
	
	public Vec2f(float x, float y)
	{
		coords = new float[2];
		
		coords[0] = x;
		coords[1] = y;
	}
	
	public Vec2f(float[] c)
	{
		coords = new float[2];
		
		coords[0] = c[0];
		coords[1] = c[1];
	}
	
	public void setCoords(float[] c)
	{
		coords[0] = c[0];
		coords[1] = c[1];
	}
	
	public void setCoords(float x, float y)
	{
		coords[0] = x;
		coords[1] = y;
	}
	
	public void setX(float x)
	{
		coords[0] = x;
	}
	
	public void setY(float y)
	{
		coords[1] = y;
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
}
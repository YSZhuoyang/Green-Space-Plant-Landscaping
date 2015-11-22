package Support;


/**
 * @author Yu Sangzhuoyang
 * @version 3.15
 */
public class Vertex
{
	private Vec3f position;
	private Vec3f normal;
	private Vec2f texCoords;
	
	private int id;
	
	public Vertex(int i)
	{
		position = new Vec3f();
		normal = new Vec3f();
		texCoords = new Vec2f();
		
		id = i;
	}
	
	public Vertex(float[] p, int i)
	{
		position = new Vec3f(p);
		normal = new Vec3f();
		texCoords = new Vec2f();
		
		id = i;
	}
	
	public Vertex(float x, float y, float z, int i)
	{
		position = new Vec3f(x, y, z);
		normal = new Vec3f();
		texCoords = new Vec2f();
		
		id = i;
	}
	
	public void calculateVertNorm(Face[] f)
	{
		float[] sum = new float[3];
		int faceCount = 0;
		
		for (int i = 0; i < f.length; i++)
		{
			if (f[i].contains(id))
			{
				sum[0] += f[i].getNormal()[0];
				sum[1] += f[i].getNormal()[1];
				sum[2] += f[i].getNormal()[2];
				
				faceCount++;
			}
		}
		
		sum[0] /= faceCount;
		sum[1] /= faceCount;
		sum[2] /= faceCount;
		
		setNormal(sum);
		normal.normalize();
	}
	
	public void mergeNormals(Vertex v)
	{
		float[] newNormal = new float[3];
		
		newNormal[0] = (getNormal()[0] + v.getNormal()[0]) / 2;
		newNormal[1] = (getNormal()[1] + v.getNormal()[1]) / 2;
		newNormal[2] = (getNormal()[2] + v.getNormal()[2]) / 2;
		
		setNormal(newNormal[0], newNormal[1], newNormal[2]);
		v.setNormal(newNormal[0], newNormal[1], newNormal[2]);
	}
	
	public void setPosition(float[] c)
	{
		position.setCoords(c);
	}
	
	public void setPosition(float x, float y, float z)
	{
		position.setCoords(x, y, z);
	}
	
	public void setNormal(float[] n)
	{
		normal.setCoords(n);
	}
	
	public void setNormal(float x, float y, float z)
	{
		normal.setCoords(x, y, z);
	}
	
	public void setTexCoords(float[] t)
	{
		texCoords.setCoords(t);
	}
	
	public void setTexCoords(float x, float y)
	{
		texCoords.setCoords(x, y);
	}
	
	//the copied position is connected to the origin, copy changes, origin changes
	public float[] getPosition()
	{
		return position.getCoords();
	}
	
	public float getX()
	{
		return position.getX();
	}
	
	public float getY()
	{
		return position.getY();
	}
	
	public float getZ()
	{
		return position.getZ();
	}
	
	//the copied normal is connected to the origin, copy changes, origin changes
	public float[] getNormal()
	{
		return normal.getCoords();
	}
	
	//the copied texCoords is connected to the origin, copy changes, origin changes
	public float[] getTexCoords()
	{
		return texCoords.getCoords();
	}
	
	public int getId()
	{
		return id;
	}
}
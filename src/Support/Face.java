package Support;


/**
 * @author Yu Sangzhuoyang
 * @version 5.05
 */
public class Face
{
	private int[] vertId;
	private Vec3f face_normal;
	
	public Face()
	{
		vertId = new int[3];
		
		face_normal = new Vec3f();
	}
	
	public Face(int a, int b, int c)
	{
		face_normal = new Vec3f();
		vertId = new int[3];
		
		vertId[0] = a;
		vertId[1] = b;
		vertId[2] = c;
	}
	
	public void setVertId(int a, int b, int c)
	{
		vertId[0] = a;
		vertId[1] = b;
		vertId[2] = c;
	}
	
	//是否需要有待商榷
	public int[] getVertices()
	{
		return vertId;
	}
	
	public float[] getNormal()
	{
		return face_normal.getCoords();
	}
	
	public void calculateFaceNorm(Vertex[] v)
	{
		float[] v1 = new float[3];
		float[] v2 = new float[3];
		float[] normal = new float[3];
		
		v1[0] = v[vertId[1]].getX() - v[vertId[0]].getX();
		v1[1] = v[vertId[1]].getY() - v[vertId[0]].getY();
		v1[2] = v[vertId[1]].getZ() - v[vertId[0]].getZ();
		
		v2[0] = v[vertId[2]].getX() - v[vertId[0]].getX();
		v2[1] = v[vertId[2]].getY() - v[vertId[0]].getY();
		v2[2] = v[vertId[2]].getZ() - v[vertId[0]].getZ();
		
		Vec3f.crossProduct(v1, v2, normal);
		
		face_normal.setCoords(normal);
		face_normal.normalize();
	}
	
	public boolean contains(int id)
	{
		if (id == vertId[0] || id == vertId[1] || id == vertId[2])
		{
			return true;
		}
		else
		{
			return false;
		}
	}
}
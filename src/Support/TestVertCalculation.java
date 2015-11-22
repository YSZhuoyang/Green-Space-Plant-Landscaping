package Support;


/**
 * @author Yu Sangzhuoyang
 * @version 3.25
 */
public class TestVertCalculation
{
	
	public TestVertCalculation() {}
	
	public static void main(String[] args)
	{
		Vertex[] v = new Vertex[3];
		
		v[0] = new Vertex(new float[]{
				1, 0, 0
		}, 0);
		
		v[1] = new Vertex(new float[]{
				0, 0, 1
		}, 1);
		
		v[2] = new Vertex(new float[]{
				2, 1, 1
		}, 2);
		
		//Face f = new Face(v[0], v[1], v[2]);
		//f.calculateFaceNorm();
		
		//System.out.println(f.getNormal()[0]);
		//System.out.println(f.getNormal()[1]);
		//System.out.println(f.getNormal()[2]);
	}
}
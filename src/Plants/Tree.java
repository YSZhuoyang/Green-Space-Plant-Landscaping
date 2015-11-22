package Plants;

import StandardObjects.Branch;
import StandardObjects.LeafBlade;
import StandardObjects.LeafBranch;

import com.jogamp.opengl.math.Matrix4;


public class Tree extends Plant
{
	private float[] modelMat;
	
	private LeafBlade leaf;
	private Branch branch;
	private LeafBranch leafBranch;
	
	public Tree(float x, float y, float z)
	{
		modelMat = new float[16];
		
		Matrix4 modelMatrix = new Matrix4();
		modelMatrix.translate(x, y, z);
		modelMat = modelMatrix.getMatrix();
	}
	
	public void attach(LeafBlade lBlade, LeafBranch lBran, Branch b)
	{
		leaf = lBlade;
		leafBranch = lBran;
		branch = b;
		
		leaf.setModelMatrix(modelMat);
		branch.setModelMatrix(modelMat);
		leafBranch.setModelMatrix(modelMat);
		
		branch.generate();
		leafBranch.generate();
		leaf.generate();
	}
	
	public void translate(float x, float y, float z)
	{
		branch.translate(x, y, z);
		leafBranch.translate(x, y, z);
		leaf.translate(x, y, z);
	}
	
	public void rotate(float angle, float x, float y, float z)
	{
		branch.rotate(angle, x, y, z);
		leafBranch.rotate(angle, x, y, z);
		leaf.rotate(angle, x, y, z);
	}
	
	public void renderDepth()
	{
		leaf.renderDepth();
		branch.renderDepth();
		leafBranch.renderDepth();
	}
	
	public void renderView()
	{
		leaf.renderView();
		branch.renderView();
		leafBranch.renderView();
	}
	
	public void destroy()
	{
		/*for (Object3D object : objectQueue)
		{
			object.destroy();
		}*/
	}
}

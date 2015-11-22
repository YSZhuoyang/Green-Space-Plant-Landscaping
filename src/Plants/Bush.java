package Plants;

import static Support.Resources.*;
import StandardObjects.Branch;
import StandardObjects.LeafBranch;
import StandardObjects.SmallLeaf;
import Support.Transformation;
import Support.Vec3f;

import com.jogamp.opengl.math.Matrix4;


/**
 * @author Yu Sangzhuoyang
 * @version 4.18
 */
public class Bush extends Plant
{
	private float[] modelMat;
	
	private Branch branch;
	private SmallLeaf sleaf;
	private LeafBranch leafBranch;
	
	private Transformation translation;
	
	public Bush(float[] s, float[] e)
	{
		modelMat = new float[16];
		
		sleaf = new SmallLeaf();
		
		leafBranch = new LeafBranch();
		leafBranch.attach(sleaf);
		
		branch = new Branch();
		branch.attach(leafBranch);
		
		translation = new Transformation();
		
		Matrix4 transMat = new Matrix4();
		transMat.translate(s[0], s[1], s[2]);
		modelMat = transMat.getMatrix();
		
		branch.setModelMatrix(modelMat);
		sleaf.setModelMatrix(modelMat);
		leafBranch.setModelMatrix(modelMat);
		
		float[] dir = {e[0] - s[0], e[1] - s[1], e[2] - s[2]};
		float[] crossDir = new float[3];
		
		Vec3f.crossProduct(dir, new float[]{0f, 1f, 0f}, crossDir);
		Vec3f.normalize(dir);
		Vec3f.normalize(crossDir);
		Vec3f.dot(crossDir, 5, crossDir);
		Vec3f.dot(crossDir, 1f / density, crossDir);
		Vec3f.dot(crossDir, width / 60f, crossDir);
		
		//System.out.println(crossDir[0] + " " + crossDir[1] + " " + crossDir[2]);
		
		for (float j = 0, n = 0; Math.abs(j) < Math.abs(e[0] - s[0]); j += dir[0] * 0.4f, n++)
		{
			for (int i = -(density - 1) / 2; i <= (density - 1) / 2; i++)
			{
				float x = j + i * crossDir[0];
				float z = n * dir[2] * 0.4f + i * crossDir[2];
				
				translation.loadIdentity();
				translation.addRotationDegreesX(0.4f);
				translation.translate(x, 0f, z);
				translation.rotate((float) Math.random() * 3f, 0f, 1.0f, 0f);
				
				branch.addBranch(translation, 0.015f, 0.015f, 2f, true);
			}
		}
		
		branch.generate();
		leafBranch.generate();
		sleaf.generate();
	}
	
	public void translate(float x, float y, float z)
	{
		branch.translate(x, y, z);
		leafBranch.translate(x, y, z);
		sleaf.translate(x, y, z);
	}
	
	public void rotate(float angle, float x, float y, float z)
	{
		branch.rotate(angle, x, y, z);
		leafBranch.rotate(angle, x, y, z);
		sleaf.rotate(angle, x, y, z);
	}
	
	public void renderDepth()
	{
		sleaf.renderDepth();
		branch.renderDepth();
		leafBranch.renderDepth();
	}
	
	public void renderView()
	{
		sleaf.renderView();
		branch.renderView();
		leafBranch.renderView();
	}
	
	public void destroy() {}
}
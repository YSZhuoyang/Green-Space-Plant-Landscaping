package LSystem;

import Plants.Plant;
import Plants.Tree;
import StandardObjects.Branch;
import StandardObjects.LeafBranch;
import StandardObjects.LeafBlade;
import StandardObjects.SmallLeaf;
//import StandardObjects.BroadLeaf;
import Support.Transformation;
import static Support.Resources.*;

import java.util.Stack;


/**
 * @author Yu Sangzhuoyang
 * @version 3.26
 */
public class Interpreter
{
	private SmallLeaf sleaf;
	private LeafBlade leaf;
	private Branch branch;
	private LeafBranch leafBranch;
	
	private LStringGenerator lsg;
	
	//private Queue<Object3D> objectQueue;
	
	public Interpreter()
	{
		lsg = new LStringGenerator();
		
		//selectedPlant = "B(0.7,0.8,5,1)";
		/*selectedPlant = "B(0.7, 0.8, 5,0)B(0.6, 0.7, 4,0)[RL(50)B(0.4, 0.6, 2,0)B(0.3, 0.4, 3,1)]"
				+ "[RH(120)RL(60)B(0.4, 0.6, 2,0)B(0.3, 0.4, 3,1)]"
				+ "[RH(240)RL(40)B(0.4, 0.6, 2,0)B(0.3, 0.4, 3,1)]";*/
		//selectedPlant = lsg.generateLString("tree");
		
		/*selectedPlant = "B(0.07, 0.08, 0.5,1)B(0.06, 0.07, 0.4,1)[RL(50)B(0.04, 0.06, 0.2,1)B(0.03, 0.04, 0.3,1)]"
				+ "[RH(120)RL(60)B(0.04, 0.06, 0.2,1)B(0.03, 0.04, 0.3,1)]"
				+ "[RH(240)RL(40)B(0.04, 0.06, 0.2,1)B(0.03, 0.04, 0.3,1)]";
		
		for (int i = 0; i < 4; i++)
		{
			selectedPlant += selectedPlant;
		}*/
		
		//System.out.println(selectedPlant);
		
		//objectQueue = new ArrayDeque<Object3D>();
	}
	
	public Plant newPlant(float[] pos)
	{
		sleaf = new SmallLeaf();
		leaf = new LeafBlade();
		
		leafBranch = new LeafBranch();
		leafBranch.attach(leaf);
		
		branch = new Branch();
		branch.attach(leafBranch);
		
		interpret();
		
		Tree tree = new Tree(pos[0], pos[1], pos[2]);
		tree.attach(leaf, leafBranch, branch);
		
		return tree;
	}
	
	public void interpret()
	{
		Stack<Transformation> transStack = new Stack<Transformation>();
		Transformation transformation = new Transformation();
		
		for (int i = 0; i < selectedPlant.length(); i++)
		{
			switch (selectedPlant.charAt(i))
			{
				case 'B':
					if (selectedPlant.charAt(i + 1) == '(')
					{
						int iClose = selectedPlant.indexOf(")", i + 1);
						String[] subs = selectedPlant.substring(i + 2, iClose).split(",");
						
						float rUpper = Float.parseFloat(subs[0]);
						float rUnder = Float.parseFloat(subs[1]);
						float l = Float.parseFloat(subs[2]);
						int withLeaf = Integer.parseInt(subs[3]);
						
						if (withLeaf == 1)
						{
							branch.addBranch(transformation, rUpper, rUnder, l, true);
							//BranchWithLeaf branch = new BranchWithLeaf(transformation, rUpper, rUnder, l, true);
							//objectQueue.add(branch);
						}
						else
						{
							branch.addBranch(transformation, rUpper, rUnder, l, false);
							//objectQueue.add(branch);
						}
						
						i = iClose;
					}
					
					break;
					
				case 'M':
					if (selectedPlant.charAt(i + 1) == '(')
					{
						int iClose = selectedPlant.indexOf(")", i + 1);
						String[] subs = selectedPlant.substring(i + 2, iClose).split(",");
						
						float width = Float.parseFloat(subs[0]);
						float length = Float.parseFloat(subs[1]);
						
						//leaf.addLeaf(transformation, width, length);
						sleaf.addLeaf(transformation, width, length);
						//objectQueue.add(leaf);
						i = iClose;
					}
					
					break;
					
				case 'L':
					if (selectedPlant.charAt(i + 1) == '(')
					{
						int iClose = selectedPlant.indexOf(")", i + 1);
						float rl = Float.parseFloat(selectedPlant.substring(i + 2, iClose));
						
						rl = rl * (float) Math.PI / 180;
						
						transformation.addRotationDegreesX(rl);
						i = iClose;
					}
					
					break;
					
				case 'H':
					if (selectedPlant.charAt(i + 1) == '(')
					{
						int iClose = selectedPlant.indexOf(")", i + 1);
						float rh = Float.parseFloat(selectedPlant.substring(i + 2, iClose));
						
						rh = rh * (float) Math.PI / 180;
						
						transformation.rotate(rh, 0.0f, 1.0f, 0.0f);
						i = iClose;
					}
					
					break;
					
				case 'U':
					if (selectedPlant.charAt(i + 1) == '(')
					{
						int iClose = selectedPlant.indexOf(")", i + 1);
						float ru = Float.parseFloat(selectedPlant.substring(i + 2, iClose));
						
						ru = ru * (float) Math.PI / 180;
						
						transformation.addRotationDegreesZ(ru);
						i = iClose;
					}
					
					break;
					
				case '[':
					transStack.push(transformation.clone());
					break;
					
				case ']':
					transformation = transStack.pop();
					break;
					
				default :
					break;
			}
		}
	}
}
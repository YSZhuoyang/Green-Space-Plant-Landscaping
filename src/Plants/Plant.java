package Plants;

import StandardObjects.Object3D;


/**
 * @author Yu Sangzhuoyang
 * @version 3.27
 */
public abstract class Plant extends Object3D
{
	public abstract void renderDepth();
	public abstract void renderView();
	public abstract void destroy();
}
package Basicsource;

import javafx.embed.swing.JFXPanel;


/**
 * Multi-sampling antialiasing is added, phone-shading is added, 
 * shadow mapping is added, particle system (rain effect) is added, 
 * particle system with compute shader is added, soft shadow mapping 
 * with percentage closer filtering is added, constants and handlers 
 * are gathered into the Resources class, transform uniforms are 
 * replaced by uniform blocks, GLObjects class is merged into the 
 * Resources class, FPS counter is added, camera is updated. User 
 * control panel is added. An object loader is added. Added models 
 * can be manipulated including move and rotation.
 * 
 * bug: cannot load .DDS texture files
 * 
 * @author Yu Sangzhuoyang
 * @version 6.14
 */
public class Main
{
	
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		Listener listener = new Listener();
		new JFXPanel();		//setup JavaFX running environment, necessary
		new ControlPanel();
		new GLDisplay(listener);
	}
}
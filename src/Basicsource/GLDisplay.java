package Basicsource;

import static Support.Resources.*;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.FPSAnimator;


/**
 * @author Yu Sangzhuoyang
 * @version 5.20
 */
public class GLDisplay
{
	private Listener listener;
	
	public GLDisplay(Listener l)
	{
		listener = l;
		
		//Run the GUI codes in the event-dispatching thread for thread safety
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				GLCapabilities caps = new GLCapabilities(null);
				GLCapabilitiesChooser chooser = new DefaultGLCapabilitiesChooser();
				
				//setup multi-sampling level
				caps.setSampleBuffers(true);
				caps.setNumSamples(8);
				
				//Create the OpenGL rendering canvas
				GLCanvas canvas = new GLCanvas(caps, chooser, null);
				//Listener listener = new Listener();
				
				canvas.addGLEventListener(listener);
				canvas.addKeyListener(listener);
				canvas.addMouseListener(listener);
				canvas.addMouseMotionListener(listener);
				canvas.setFocusable(true);
				canvas.requestFocus();
				canvas.setPreferredSize(new Dimension(CANVAS_WIDTH, CANVAS_HEIGHT));
				
	            //Create a animator that drives canvas' display() at the specified FPS.
	            final FPSAnimator animator = new FPSAnimator(canvas, FPS, true);
	            
	            //Create the top-level container
	            final Frame frame = new Frame(); // Swing's JFrame or AWT's Frame
	            frame.setLocation(CANVAS_LOCATION_X, CANVAS_LOCATION_Y);
	            //frame.getContentPane().add(canvas);
	            frame.add(canvas);
	            frame.addWindowListener(new WindowAdapter()
	            {
	               public void windowClosing(WindowEvent e)
	               {
	                  //Use a dedicate thread to run the stop() to ensure that the
	                  //animator stops before program exits.
	                  new Thread()
	                  {
	                     public void run()
	                     {
	                        if (animator.isStarted())
	                       	{
	                        	animator.stop();
	                        }
	                        
	                        System.exit(0);
	                     }
	                  }.start();
	               }
	            });
	            frame.setTitle(TITLE);
	            frame.pack();
	            frame.setVisible(true);
	            animator.start();
			}
		});
	}
}
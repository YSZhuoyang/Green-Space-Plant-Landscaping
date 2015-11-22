package Basicsource;

import Support.Camera;
import static Support.Resources.*;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

//import javafx.event.ActionEvent;
//import javafx.event.EventHandler;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;


/**
 * @author Yu Sangzhuoyang
 * @version 4.19
 */
public class Listener implements GLEventListener, KeyListener, MouseListener, MouseMotionListener//, EventHandler<ActionEvent>
{
	private Painter painter;
	//private GUITest controlPane;
	
	public Listener()
	{
		painter = new Painter();
	}
	
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height)
	{
		setGLContext(drawable);
		
		if (height == 0)
		{
			height = 1;
		}
		
		setDisplaySize(width, height);
		
		float aspect = (float) width / height;
		
		Camera.resetMatrices(aspect);
	}
	
	public void init(GLAutoDrawable drawable)
	{
		setGLContext(drawable);
		
		drawable.getAnimator().setUpdateFPSFrames(10, null);
		
		painter.setup();
	}
	
	public void display(GLAutoDrawable drawable)
	{
		setGLContext(drawable);
		
		FPSCounter = (int) drawable.getAnimator().getLastFPS();
		
		System.out.println(FPSCounter);
		
		painter.render();
	}
	
	public void dispose(GLAutoDrawable drawable)
	{
		//painter.destroy();
	}
	
	public void keyPressed(KeyEvent e)
	{
		painter.keyEvents(e);
	}
	
	public void keyReleased(KeyEvent e) {}
	
	public void keyTyped(KeyEvent e) {}
	
	public void mouseClicked(MouseEvent e)
	{
		Camera.setScreenPos(e.getX(), e.getY());
		
		if (state == State.TREEMODELSELECTED)
		{
			state = State.MOUSECLICKED;
		}
	}
	
	public void mouseEntered(MouseEvent e) {}
	
	public void mouseExited(MouseEvent e) {}
	
	public void mousePressed(MouseEvent e)
	{
		System.out.println("start: " + e.getX() + " " + e.getY());
		
		Camera.setStartPos(e.getX(), e.getY());
	}
	
	public void mouseReleased(MouseEvent e)
	{
		System.out.println("end: " + e.getX() + " " + e.getY());
		
		Camera.setEndPos(e.getX(), e.getY());
		
		if (Camera.getScreenStartPos()[0] != Camera.getScreenEndPos()[0] 
				|| Camera.getScreenStartPos()[1] != Camera.getScreenEndPos()[1] )
		{
			state = State.MOUSEDRAGGED;
		}
	}
	
	public void mouseMoved(MouseEvent e) {}
	
	public void mouseDragged(MouseEvent e) {}
	
	public void mouseWheelMoved(MouseEvent e) {}
	
	/*public void handle(ActionEvent event)
	{
		//gui.isbuttonClicked(event.getSource());
	}*/
}
package Support;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLAutoDrawable;


/**
 * Textures should be put into this class.
 * 
 * @author Yu Sangzhuoyang
 * @version 6.14
 */
public final class Resources
{
	//final static fields
	public static final int SHADOWMAP_WIDTH = 2048;
	public static final int SHADOWMAP_HEIGHT = 2048;
	public static final int CANVAS_WIDTH = 625;				//initial width of canvas
	public static final int CANVAS_HEIGHT = 500;			//initial height of canvas
	public static final int CANVAS_LOCATION_X = 180;		//initial location of canvas
	public static final int CANVAS_LOCATION_Y = 80;			//initial location of canvas
	public static final int CONTROLPANEL_WIDTH = 325;		//initial width of control panel
	public static final int CONTROLPANEL_HEIGHT = 500;		//initial height of control panel
	public static final int CONTROLPANEL_LOCATION_X = 825;	//initial location of control panel
	public static final int CONTROLPANEL_LOCATION_Y = 80;	//initial location of control panel
	
	public static final int FPS = 60;						//animator's target frames per second
	public static final String TITLE = "Green Space v6.14";
	
	//static fields
	public static GL4 gl;
	
	public static int displayWidth;
	public static int displayHeight;
	
	public static int FPSCounter;
	
	public static boolean isLightOn;
	public static boolean isRaining;
	public static boolean isSkyBoxTurnedOn = true;
	public static boolean isfieldTurnedOn = true;
	
	public static String[] plantTopologies = new String[6];
	public static String selectedPlant = "";
	
	public static String objFilePath = "";
	public static String texFilePath = "";
	
	public static int density = 5;
	public static int width = 60;
	
	public static Color color = Color.GREEN;
	public static State state = State.RENDERING;
	
	public enum Color
	{
		GREEN, PURPLE
	}
	
	public enum Perspective
	{
		TOPVIEW, FREELOOK
	}
	
	public enum State
	{
		RENDERING, MOUSECLICKED, MOUSEDRAGGED, 
		NEWMODELIMPORTED, TREEMODELSELECTED
	}
	
	private Resources() {}
	
	public static void setDisplaySize(int w, int h)
	{
		displayWidth = w;
		displayHeight = h;
	}
	
	//get the OpenGL graphics context
	public static void setGLContext(GLAutoDrawable drawable)
	{
		gl = drawable.getGL().getGL4();
	}
	
	public static void initPlantModels()
	{
		for (int i = 0; i < plantTopologies.length; i++)
		{
			plantTopologies[i] = loadFiles("data/" + (i + 3) + ".txt");
		}
	}
	
	public static String loadFiles(String filePath)
	{
		String fileContent = "";
		String line = "";
		
		try
		{
			FileReader fr = new FileReader(filePath);
			BufferedReader br = new BufferedReader(fr);
			
			while ((line = br.readLine()) != null)
			{
				fileContent += line;
			}
			
			br.close();
			fr.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		return fileContent;
	}
}
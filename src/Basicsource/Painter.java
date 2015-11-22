package Basicsource;

import Support.Camera;
import Support.Light;
import Support.ObjectLoader;
import Support.Rain;
import Support.Skybox;
import Support.Terrain;
import LSystem.Interpreter;
import Plants.Bush;
import StandardObjects.Grass;
import StandardObjects.Object3D;
import static Support.Resources.*;

import java.awt.event.*;
import java.nio.IntBuffer;
import java.util.ArrayDeque;
import java.util.Stack;

import com.jogamp.opengl.GL4;
import com.jogamp.opengl.util.GLBuffers;

import static java.awt.event.KeyEvent.*;


/**
 * @author Yu Sangzhuoyang
 * @version 6.14
 */
public class Painter
{
	private Perspective perspective;
	private Interpreter interpreter;
	private ObjectLoader objectLoader;
	
	private Grass grass;
	private Terrain terrain;
	private Skybox skybox;
	private Rain rain;
	private Object3D selectedModel;
	
	private IntBuffer fboBuff;
	private IntBuffer depthTexBuff;
	
	private ArrayDeque<Object3D> objectQueue;
	private Stack<Object3D> undo;
	
	public Painter()
	{
		perspective = Perspective.FREELOOK;
		interpreter = new Interpreter();
		objectLoader = new ObjectLoader();
		selectedModel = null;
		
		skybox = new Skybox();
		terrain = new Terrain();
		grass = new Grass();
		rain = new Rain(3000);
		
		fboBuff = GLBuffers.newDirectIntBuffer(1);
		depthTexBuff = GLBuffers.newDirectIntBuffer(1);
		
		objectQueue = new ArrayDeque<Object3D>();
		undo = new Stack<Object3D>();
		
		initPlantModels();
	}
	
	protected void setup()
	{
		Camera.init();
		Light.init();
		
		gl.glEnable(GL4.GL_DEPTH_TEST); //enable depth testing
		gl.glEnable(GL4.GL_MULTISAMPLE);//enable multi-sampling antialiasing
		//gl.glEnable(GL4.GL_BLEND);
		
		gl.glDepthFunc(GL4.GL_LEQUAL);  //the type of depth test to do
		
		// Linear filter is more compute-intensive
        // Use linear filter if image is larger than the original texture
		gl.glTexParameteri(GL4.GL_TEXTURE_2D, GL4.GL_TEXTURE_MAG_FILTER, GL4.GL_LINEAR);
        // Use linear filter if image is smaller than the original texture
		gl.glTexParameteri(GL4.GL_TEXTURE_2D, GL4.GL_TEXTURE_MIN_FILTER, GL4.GL_LINEAR_MIPMAP_LINEAR);
		
		//Blending
		//Used blending function based on source alpha value
		//gl.glEnable(GL4.GL_BLEND);
		//gl.glBlendFunc(GL4.GL_SRC_ALPHA, GL4.GL_ONE);
		//gl.glDisable(GL_BLEND_COLOR);
		//gl.glEnable(GL_DEPTH_TEST);
		
		grass.initialize();
		terrain.initialize();
		skybox.initialize();
		rain.initSystem();
		
		setupDepthBuffer();
	}
	
	protected void setupDepthBuffer()
	{
		//bind framebuffer for shadow mapping
		gl.glGenFramebuffers(1, fboBuff);
		gl.glBindFramebuffer(GL4.GL_FRAMEBUFFER, fboBuff.get(0));
		
		gl.glGenTextures(1, depthTexBuff);
		gl.glBindTexture(GL4.GL_TEXTURE_2D, depthTexBuff.get(0));
		gl.glTexStorage2D(GL4.GL_TEXTURE_2D, 1, GL4.GL_DEPTH_COMPONENT32F, SHADOWMAP_WIDTH, SHADOWMAP_HEIGHT);
		gl.glTexParameteri(GL4.GL_TEXTURE_2D, GL4.GL_TEXTURE_MAG_FILTER, GL4.GL_LINEAR);
		gl.glTexParameteri(GL4.GL_TEXTURE_2D, GL4.GL_TEXTURE_MIN_FILTER, GL4.GL_LINEAR);
		gl.glTexParameteri(GL4.GL_TEXTURE_2D, GL4.GL_TEXTURE_COMPARE_MODE, GL4.GL_COMPARE_REF_TO_TEXTURE);
		gl.glTexParameteri(GL4.GL_TEXTURE_2D, GL4.GL_TEXTURE_COMPARE_FUNC, GL4.GL_LEQUAL);
		gl.glTexParameteri(GL4.GL_TEXTURE_2D, GL4.GL_TEXTURE_WRAP_S, GL4.GL_CLAMP_TO_EDGE);
		gl.glTexParameteri(GL4.GL_TEXTURE_2D, GL4.GL_TEXTURE_WRAP_T, GL4.GL_CLAMP_TO_EDGE);
		gl.glFramebufferTexture(GL4.GL_FRAMEBUFFER, GL4.GL_DEPTH_ATTACHMENT, depthTexBuff.get(0), 0);
		
		gl.glDrawBuffer(GL4.GL_NONE);
		
		if(gl.glCheckFramebufferStatus(GL4.GL_FRAMEBUFFER) != GL4.GL_FRAMEBUFFER_COMPLETE)
			System.out.println(gl.glCheckFramebufferStatus(GL4.GL_FRAMEBUFFER));
		
		gl.glBindTexture(GL4.GL_TEXTURE_2D, 0);
		gl.glBindFramebuffer(GL4.GL_FRAMEBUFFER, 0);
	}
	
	protected void switchPerspective()
	{
		if (perspective == Perspective.TOPVIEW)
		{
			Camera.resetFreeLook();
			
			perspective = Perspective.FREELOOK;
		}
		else
		{
			Camera.resetTopView();
			
			perspective = Perspective.TOPVIEW;
		}
	}
	
	private void paint()
	{
		switch (state)
		{
			case RENDERING:
				break;
				
			case MOUSECLICKED:
				float[] pos = Camera.getClickPos();
				
				if (inBounds(pos))
				{
					pos[1] = 0f;
					
					objectQueue.add(interpreter.newPlant(pos));
					selectedModel = objectQueue.peekLast();
				}
				
				state = State.RENDERING;
				break;
				
			case MOUSEDRAGGED:
				float[] startPos = Camera.getPressPos();
				float[] endPos = Camera.getReleasePos();
				
				if (inBounds(startPos) && inBounds(endPos))
				{
					startPos[1] = 0f;
					endPos[1] = 0f;
					
					objectQueue.add(new Bush(startPos, endPos));
					selectedModel = objectQueue.peekLast();
				}
				
				state = State.RENDERING;
				break;
				
			case NEWMODELIMPORTED:
				selectedModel = objectLoader.importModel(objFilePath, texFilePath);
				objectQueue.add(selectedModel);
				
				state = State.RENDERING;
				
			default:
				break;
		}
	}
	
	protected boolean inBounds(float[] pos)
	{
		if (-25f < pos[0] && pos[0] < 25f && -25f < pos[2] && pos[2] < 25f)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	protected void renderDepth()
	{
		gl.glBindFramebuffer(GL4.GL_FRAMEBUFFER, fboBuff.get(0));
		gl.glViewport(0, 0, SHADOWMAP_WIDTH, SHADOWMAP_HEIGHT);
		
		gl.glEnable(GL4.GL_POLYGON_OFFSET_FILL);
		gl.glPolygonOffset(2.0f, 4.0f);
		
		gl.glClear(GL4.GL_COLOR_BUFFER_BIT | GL4.GL_DEPTH_BUFFER_BIT);
		
		grass.renderDepth();
		terrain.renderDepth();
		
		for (Object3D obj : objectQueue)
		{
			obj.renderDepth();
		}
		
		gl.glDisable(GL4.GL_POLYGON_OFFSET_FILL);
	}
	
	protected void render()
	{
		Camera.look();
		Light.illuminate();
		
		// Lighting
		/*if (isLightOn)
	    {
			Light.illuminate();
	    }
	    else
	    {
	    	
	    }*/
		
		renderDepth();
		
		//render to the screen
		gl.glBindFramebuffer(GL4.GL_FRAMEBUFFER, 0);
		gl.glViewport(0, 0, displayWidth, displayHeight);
		
		gl.glClear(GL4.GL_COLOR_BUFFER_BIT | GL4.GL_DEPTH_BUFFER_BIT);
		gl.glClearColor(1, 1, 1, 1);
		gl.glActiveTexture(GL4.GL_TEXTURE5);
		gl.glBindTexture(GL4.GL_TEXTURE_2D, depthTexBuff.get(0));
		
		if (isSkyBoxTurnedOn)
		{
			skybox.renderView();
		}
		
		if (isfieldTurnedOn)
		{
			terrain.renderView();
			grass.renderView();
		}
		
		rain.renderView();
		
		paint();
		
		for (Object3D obj : objectQueue)
		{
			obj.renderView();
		}
	}
	
	public void destroy()
	{
		
	}
	
	protected void keyEvents(KeyEvent e)
	{
		int keyCode = e.getKeyCode();
		
		switch (keyCode)
		{
			case VK_1:
				isSkyBoxTurnedOn = !isSkyBoxTurnedOn;
				break;
			case VK_2:
				isfieldTurnedOn = !isfieldTurnedOn;
				break;
			case VK_R:
				if (isRaining)
				{
					rain.turnOff();
				}
				else
				{
					rain.turnOn();
				}
				
				isRaining = !isRaining;
				break;
			case VK_S:
				switchPerspective();
				break;
			case VK_A:
				Light.moveLeft();
				break;
			case VK_D:
				Light.moveRight();
				break;
			case VK_I:
				if (selectedModel != null)
				{
					selectedModel.translate(0f, 0f, -0.1f);
				}
				
				break;
			case VK_K:
				if (selectedModel != null)
				{
					selectedModel.translate(0f, 0f, 0.1f);
				}
				
				break;
			case VK_J:
				if (selectedModel != null)
				{
					selectedModel.translate(-0.1f, 0f, 0f);
				}
				
				break;
			case VK_L:
				if (selectedModel != null)
				{
					selectedModel.translate(0.1f, 0f, 0f);
				}
				
				break;
			case VK_E:
				if (selectedModel != null)
				{
					selectedModel.rotate(0.1f, 0f, 1.0f, 0f);
				}
				
				break;
			//case VK_L:
			//	isLightOn = !isLightOn;
			//	break;
			case VK_PAGE_UP:
				Camera.moveUpward();
				break;
			case VK_PAGE_DOWN:
				Camera.moveDownward();
				break;
			case VK_UP:
				Camera.moveForward();
				break;
			case VK_DOWN:
				Camera.moveBackward();
				break;
			case VK_LEFT:
				if (perspective == Perspective.FREELOOK)
				{
					Camera.rotate(0.1f);
				}
				
				break;
			case VK_RIGHT:
				if (perspective == Perspective.FREELOOK)
				{
					Camera.rotate(-0.1f);
				}
				
				break;
			case VK_C:
				objectQueue.clear();
				undo.clear();
				break;
			case VK_Z:
				if (!objectQueue.isEmpty())
				{
					undo.push(objectQueue.removeLast());
					selectedModel = objectQueue.peekLast();
				}
				
				break;
			case VK_Y:
				if (!undo.isEmpty())
				{
					objectQueue.add(undo.pop());
					selectedModel = objectQueue.peekLast();
				}
				
				break;
		}
	}
}
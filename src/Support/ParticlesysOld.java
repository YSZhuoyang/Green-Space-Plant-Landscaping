package Support;

import StandardObjects.Object3D;

import static Support.Resources.*;

import com.jogamp.opengl.GL4;
import com.jogamp.opengl.util.GLBuffers;


/**
 * This a particle system where the particle data initialization, 
 * update, and dying are performed with CPU calculation.
 * 
 * @author Yu Sangzhuoyang
 * @version 4.19
 */
public abstract class ParticlesysOld extends Object3D
{
	protected Particle[] particles;
	protected int birthRate;
	protected float speed;
	protected Vec3f dir;
	protected int currentCount;
	
	private int particleCount;
	private int startCount;
	private int newParticles;
	private float timeBetweenAni;
	
	private int verPosLoc;
	
	private boolean shutDown;
	
	public ParticlesysOld(int pc)
	{
		particleCount = pc;
		startCount = 0;
		timeBetweenAni = 1f / 60f;
		speed = 1f;
		dir = new Vec3f(0f, -1f, 0f);
		shutDown = true;
		
		birthRate = 180;
		
		particles = new Particle[particleCount];
		
		for (int i = 0; i < particleCount; i++)
		{
			particles[i] = new Particle();
		}
		
		vertBuffSize = particles.length * 4;		//number of particles multiplied by 4
		
		vboCount = 1;
		
		vaoBuff = GLBuffers.newDirectIntBuffer(1);
		vboBuff = GLBuffers.newDirectIntBuffer(vboCount);
		vertBuff = GLBuffers.newDirectFloatBuffer(vertBuffSize);
		
		verPosLoc = 0;
	}
	
	public void initSystem()
	{
		initViewProgram();
		setupBuffers();
		loadTexture();
		
		for (int i = 0; i < startCount; i++)
		{
			initParticle(i);
		}
		
		for (int i = startCount; i < particleCount; i++)
		{
			particles[i].lifespan = 0;
		}
		
		shutDown = false;
	}
	
	protected void animate()
	{
		newParticles += birthRate * timeBetweenAni;
		
		float[] distance = new float[3];
		
		if (newParticles > particleCount)
		{
			newParticles = particleCount;
		}
		
		for (int i = 0; i < particleCount; i++)
		{
			if (particles[i].lifespan > 0)
			{
				particles[i].age += timeBetweenAni;
				
				if (particles[i].age > particles[i].lifespan)
				{
					particles[i].lifespan = 0;
				}
				else
				{
					distance = particles[i].velocity.dot(timeBetweenAni);
					
					particles[i].position.add(distance);
				}
			}
			else
			{
				if (!shutDown && newParticles > 0)
				{
					initParticle(i);
					newParticles--;
				}
			}
		}
		
		resetBufferData();
	}
	
	//setup the positions and sizes of particles
	protected void resetBufferData()
	{
		vertBuff.clear();
		currentCount = 0;
		
		for (int i = 0; i < particleCount; i++)
		{
			if (particles[i].lifespan > 0)
			{
				vertBuff.put(particles[i].position.getCoords());
				vertBuff.put(particles[i].size);
				
				currentCount++;
			}
		}
		
		vertBuff.rewind();
	}
	
	protected void setupBuffers()
	{
		//generate vao
		gl.glGenVertexArrays(1, vaoBuff);
		gl.glBindVertexArray(vaoBuff.get(0));
		
		//bind the particle position & size buffer
		gl.glGenBuffers(vboCount, vboBuff);
		gl.glBindBuffer(GL4.GL_ARRAY_BUFFER, vboBuff.get(0));
		gl.glBufferData(GL4.GL_ARRAY_BUFFER, vertBuffSize * Float.SIZE / 8, null, GL4.GL_STATIC_DRAW);
		gl.glVertexAttribPointer(verPosLoc, 4, GL4.GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(verPosLoc);
		gl.glVertexAttribDivisor(verPosLoc, 1);
	}
	
	public void turnOn()
	{
		shutDown = false;
	}
	
	public void turnOff()
	{
		shutDown = true;
	}
	
	protected void setupVertices() {}
	protected void loadTexture() {}
	public void destroy() {}
	
	public abstract void initParticle(int i);
}
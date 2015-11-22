package Support;

import StandardObjects.Object3D;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import com.jogamp.opengl.math.Matrix4;
import com.jogamp.opengl.util.GLBuffers;


/**
 * @author Yu Sangzhuoyang
 * @version 6.04
 */
public class ObjectLoader
{
	private FloatBuffer vertBuff;
	private FloatBuffer normBuff;
	private FloatBuffer texBuff;
	
	private ArrayList<Vec3f> vCoords;
	private ArrayList<Vec3f> normals;
	private ArrayList<Vec2f> texCoords;
	
	private ArrayList<Integer> indexVertCoords;
	private ArrayList<Integer> indexNormals;
	private ArrayList<Integer> indexTexCoords;
	
	/**
	 * 
	 */
	public ObjectLoader()
	{
		vCoords = new ArrayList<Vec3f>();
		normals = new ArrayList<Vec3f>();
		texCoords = new ArrayList<Vec2f>();
		
		indexVertCoords = new ArrayList<Integer>();
		indexNormals = new ArrayList<Integer>();
		indexTexCoords = new ArrayList<Integer>();
	}
	
	public Object3D importModel(String objFilePath, String textureFilePath)
	{
		Object3D model = new Object3D();
		
		vCoords.clear();
		normals.clear();
		texCoords.clear();
		indexVertCoords.clear();
		indexNormals.clear();
		indexTexCoords.clear();
		
		loadFile(objFilePath);
		
		model.setVertCoords(vertBuff, indexVertCoords.size() * 3);
		model.setNormals(normBuff, indexVertCoords.size() * 3);
		model.setTextCoords(texBuff, indexVertCoords.size() * 2);
		
		Matrix4 modelMat = new Matrix4();
		modelMat.loadIdentity();
		model.setModelMatrix(modelMat.getMatrix());
		
		//需要放到render阶段执行
		model.initialize();
		//model.setupTextures(textureFilePath);
		model.loadTexture(textureFilePath);
		
		return model;
	}
	
	public void loadFile(String filePath)
	{
		if (!checkFile(filePath))
		{
			System.out.println("Incorrect obj file!");
			
			return;
		}
		
		String line = "";
		String lineHeader = "";
		
		try
		{
			FileReader fr = new FileReader(filePath);
			BufferedReader br = new BufferedReader(fr);
			
			while ((line = br.readLine()) != null)
			{
				String[] splitted = line.split(" ");
				lineHeader = splitted[0];
				
				if (lineHeader.equals("v"))
				{
					readVertices(splitted);
				}
				else if (lineHeader.equals("vt"))
				{
					readTexCoords(splitted);
				}
				else if (lineHeader.equals("vn"))
				{
					readNormals(splitted);
				}
				else if (lineHeader.equals("f"))
				{
					readFaces(splitted);
				}
			}
			
			br.close();
			fr.close();
			
			vertBuff = GLBuffers.newDirectFloatBuffer(indexVertCoords.size() * 3);
			normBuff = GLBuffers.newDirectFloatBuffer(indexNormals.size() * 3);
			texBuff = GLBuffers.newDirectFloatBuffer(indexTexCoords.size() * 2);
			
			for (int i = 0; i < indexVertCoords.size(); i++)
			{
				vertBuff.put(vCoords.get(indexVertCoords.get(i) - 1).getCoords());
				normBuff.put(normals.get(indexNormals.get(i) - 1).getCoords());
				texBuff.put(texCoords.get(indexTexCoords.get(i) - 1).getCoords());
			}
			
			vertBuff.rewind();
			normBuff.rewind();
			texBuff.rewind();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public boolean checkFile(String filePath)
	{
		String line = "";
		String lineHeader = "";
		
		boolean containV = false;
		boolean containVN = false;
		boolean containVT = false;
		boolean containF = false;
		
		try
		{
			FileReader fr = new FileReader(filePath);
			BufferedReader br = new BufferedReader(fr);
			
			while ((line = br.readLine()) != null)
			{
				String[] splitted = line.split(" ");
				lineHeader = splitted[0];
				
				if (lineHeader.equals("v"))
				{
					containV = true;
				}
				else if (lineHeader.equals("vt"))
				{
					containVT = true;
				}
				else if (lineHeader.equals("vn"))
				{
					containVN = true;
				}
				else if (lineHeader.equals("f"))
				{
					containF = true;
				}
			}
			
			br.close();
			fr.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		return containV && containVN && containVT && containF;
	}
	
	public void readVertices(String[] v)
	{
		Vec3f vec3f = new Vec3f(Float.parseFloat(v[1]), 
				Float.parseFloat(v[2]), 
				Float.parseFloat(v[3]));
		
		//System.out.println(v[1] + "   " + v[2] + "   " + v[3]);
		
		vCoords.add(vec3f);
	}
	
	public void readNormals(String[] vn)
	{
		Vec3f vec3f = new Vec3f(Float.parseFloat(vn[1]), 
				Float.parseFloat(vn[2]), 
				Float.parseFloat(vn[3]));
		
		//System.out.println(vn[1] + "   " + vn[2] + "   " + vn[3]);
		
		normals.add(vec3f);
	}
	
	public void readTexCoords(String[] vt)
	{
		Vec2f vec2f = new Vec2f(Float.parseFloat(vt[1]), 
				Float.parseFloat(vt[2]));
		
		//System.out.println(vt[1] + "   " + vt[2]);
		
		texCoords.add(vec2f);
	}
	
	public void readFaces(String[] f)
	{
		String[] indices;
		
		for (int i = 1; i < 4; i++)
		{
			indices = f[i].split("\\/");
			
			//System.out.println(indices[0] + "   " + indices[1] + "   " + indices[2]);
			
			indexVertCoords.add(Integer.parseInt(indices[0]));
			indexTexCoords.add(Integer.parseInt(indices[1]));
			//if (!indices[1].isEmpty())
			indexNormals.add(Integer.parseInt(indices[2]));
		}
	}
}
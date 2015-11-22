package LSystem;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;


/**
 * Identify and store the axiom string and a set of growth rules.
 * @author Yu Sangzhuoyang
 * @version 2014.12.05
 */

public class LexicalAnalyzer
{
	private String w;                           	    //公理
	private String log;									//日志记录
	
	private int iterateNum;								//迭代次数
	
	private LinkedHashMap<String, String> production_hm;//存储产生式
	private Parser parser;								//string interpreter which is used to parse the code
	
	/**
	 * 构造函数，变量初始化
	 * @param none
	 * @return none
	 */
	public LexicalAnalyzer()
	{
		initialize();
	}
	
	public void initialize()
	{
		log = "";
		w = "";
		iterateNum = 0;
		
		parser = new Parser();
		production_hm = new LinkedHashMap<String, String>();
	}
	
	public void load(String filePath)
	{
		String fileContent = "";
		String initStr = "";
		String symbol = "";
		String line = "";
		String[] rowStr;
		
		StringBuffer succ = new StringBuffer();
		
		try
		{
			FileReader fr = new FileReader(filePath);
			BufferedReader br = new BufferedReader(fr);
			
			while ((line = br.readLine()) != null)
			{
				fileContent += line + "\n";
			}
			
			br.close();
			fr.close();
			
			fileContent = fileContent.replaceAll(" ", "");
			fileContent = fileContent.replaceAll("	", "");
			
			rowStr = fileContent.split("\n");
			
			for (int i = 0; i < rowStr.length; i++)
			{
				if (rowStr[i].contains("Axiom"))				//存入公理
				{
					initStr = fileContent.substring(fileContent.indexOf("Axiom"), fileContent.indexOf(";", fileContent.indexOf("Axiom")));
					w = initStr.substring(initStr.indexOf("==>") + 3);
				}
				else if (rowStr[i].contains("Iterate"))
				{
					iterateNum = Integer.parseInt(rowStr[i].substring(8));
				}
				else if (rowStr[i].contains("==>"))				//存入产生式
				{
					symbol = (rowStr[i].substring(0, rowStr[i].indexOf("==>")));
					
					while (i + 1 < rowStr.length && !rowStr[i + 1].contains("==>"))
					{
						i++;
						succ.append(rowStr[i] + "\n");
					}
					
					production_hm.put(symbol, succ.toString());
					succ.delete(0, succ.length());
				}
			}
			
			parser.setUp(w, production_hm);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public void process()
	{
		for (int i = 0; i < iterateNum; i++)
		{
			System.out.println(i);		//print out iteration number
			
			parser.parse();
			
			System.out.println(parser.getLString());		//print out L-string
		}
	}
	
	public String getLString()
	{
		return parser.getLString();
	}
	
	public boolean isNumeric(String s)
	{
		if (s == null || s.equals(""))
		{
			return false;
		}
		else
		{
			for (int i = 1; i < s.length(); i++)
			{
				if (!Character.isDigit(s.charAt(i)) && !(s.charAt(i) == '.'))
				{
					return false;
				}
			}
		}
		
		return true;
	}
	
	public void attachViews()
	{
		
	}
	
	public void initializeViews()
	{
		
	}
	
	public void notifyViews()
	{
		
	}
	
	/**
	 * @param none
	 * @return String type
	 * 返回字符串替换日志记录
	 */
	public String getLog()
	{
		return log;
	}
}
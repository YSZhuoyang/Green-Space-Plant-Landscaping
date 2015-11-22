package LSystem;

import java.util.*;
import javax.script.*;


/**
 * @author Yu Sangzhuoyang
 * @version 1.15
 */
public class Parser
{
	private int row;									//行数
	private double pro;									//计算产生式的概率叠加
	private double p_ran;								//随机数作为产生是概率

	private boolean undone;								//表示if语句中是否已执行过某个条件下的语句
	
	private String w;                           	    //公理
	private String[] rowStr;							//存放被划分的每一行
	
	private Object[] key;
	
	private LinkedHashMap<String, String> production_hm;//存储产生式
	
	/**
	 * @return none
	 * 构造函数，变量初始化
	 */
	public Parser()
	{
		w = "";
		undone = true;
	}
	
	/**
	 * @param axiom
	 * @param production_hm
	 * @return none
	 * 存储各个模块，包括前驱字符串，产生式
	 */
	public void setUp(String axiom, LinkedHashMap<String,String> productions)
	{
		w = axiom;
		production_hm = productions;
		key = production_hm.keySet().toArray();
	}
	
	/**
	 * 进行字符串迭代替换，遍历前驱字符串，查找相应产生式，将产生式经过相应整理和替换后
	 * 替换前驱字符串中的字符。
	 * @param none
	 * @return none
	 */
	public void parse()
	{
		int strIndex = 0;									//公理中的单个字符的下标号
		
		String formalParamStr = null;
		String actualParamStr = null;
		String pn = null;
		
		String[] formalParam = null;
		String[] actualParam = null;
		
		String symbolWithVar = "";
		String symbol = "";
		
		for (int k = 0; k < key.length; k++)
		{
			symbolWithVar = (String) key[k];
			symbol = symbolWithVar.substring(0, 1);
			
			if (symbolWithVar.contains("("))
			{
				formalParamStr = symbolWithVar.substring(symbolWithVar.indexOf("(") + 1, symbolWithVar.indexOf(")"));
				formalParam = formalParamStr.split(",");
			}
			
			pn = production_hm.get(key[k]);
			
			while (strIndex < w.length())
			{
				if (w.substring(strIndex, strIndex + 1).equals(symbol))
				{
					if (strIndex + 1 < w.length() && w.charAt(strIndex + 1) == '(')
					{
						actualParamStr = w.substring(strIndex + 2, w.indexOf(")", strIndex));
						actualParam = actualParamStr.split(",");
						
						for (int n = formalParam.length - 1; n >= 0; n--)
						{
							pn = pn.replaceAll(formalParam[n], actualParam[n]);
						}
					}
					
					pn = executeCode(pn);
					pn = calculateStr(pn);
					pn = pn.replaceAll("\\{", "");
					pn = pn.replaceAll("\\}", "");
					//pn = pn.replaceAll("\\;", "");
					
					w = w.substring(0, strIndex) + pn + w.substring(w.indexOf(")", strIndex) + 1, w.length());
					
					strIndex = strIndex + pn.length() - 1;
				}
				
				pn = production_hm.get(key[k]);
				strIndex++;
			}
			
			strIndex = 0;
		}
	}
	
	/**
	 * @param p 产生式字符串
	 * @return String type
	 * 按行重新整理产生式，并执行if条件语句进行判断
	 */
	public String executeCode(String p)
	{
		StringBuffer succ = new StringBuffer();
		row = 0;
		rowStr = p.split("\n");
		
		while (row < rowStr.length)
		{
			rowStr[row] = runFunction(rowStr[row]);
			
			if (rowStr[row].contains("if"))
			{
				succ.append(ifStr());
			}
			else if (rowStr[row].contains("else"))
			{
				succ.append(elseStr());
			}
			else if (rowStr[row].contains("for"))
			{
				succ.append(forLoop());
			}
			else
			{
				succ.append(rowStr[row]);
			}
			
			row++;
		}
		
		return succ.toString();
	}
	
	/**
	 * @param p 产生式字符串
	 * @return String type
	 * 执行if条件语句判断是否满足条件，若满足则执行内部语句
	 */
	public StringBuffer ifStr()
	{
		StringBuffer succ = new StringBuffer();
		String condition = rowStr[row].substring(rowStr[row].indexOf("(") + 1, rowStr[row].length() - 1);
		
		if (!rowStr[row].contains("elseif"))
		{
			undone = true;
			pro = 0;
			p_ran = Math.random();
		}
		
		if (ifCondition(condition) && undone)
		{
			row++;
			
			while (row < rowStr.length && !rowStr[row].contains("}"))
			{
				rowStr[row] = runFunction(rowStr[row]);
				
				if (rowStr[row].contains("if"))
				{
					succ.append(ifStr());
				}
				else if (rowStr[row].contains("else"))
				{
					succ.append(elseStr());
				}
				else if (rowStr[row].contains("for"))
				{
					succ.append(forLoop());
				}
				else
				{
					succ.append(rowStr[row]);
				}
				
				row++;
			}
			
			undone = false;
			
			return succ;
		}
		else
		{
			int num_brace = 1;
			
			while (!rowStr[row].contains("{"))
			{
				row++;
			}
			
			while (num_brace != 0)
			{
				row++;
				
				if (rowStr[row].contains("{"))
				{
					num_brace++;
				}
				if (rowStr[row].contains("}"))
				{
					num_brace--;
				}
			}
			
			return succ;
		}
	}
	
	/**
	 * @param p 产生式字符串
	 * @return String type
	 * 执行else条件语句判断是否满足条件，若满足则执行内部语句
	 */
	public StringBuffer elseStr()
	{
		StringBuffer succ = new StringBuffer();
		
		row++;
		
		if (undone)
		{
			while (row < rowStr.length && !rowStr[row].contains("}"))
			{
				rowStr[row] = runFunction(rowStr[row]);
				
				if (rowStr[row].contains("if"))
				{
					succ.append(ifStr());
				}
				else if (rowStr[row].contains("else"))
				{
					succ.append(elseStr());
				}
				else if (rowStr[row].contains("for"))
				{
					succ.append(forLoop());
				}
				else
				{
					succ.append(rowStr[row]);
				}
				
				row++;
			}
			
			undone = false;
			
			return succ;
		}
		else
		{
			int num_brace = 1;
			
			while (!rowStr[row].contains("{"))
			{
				row++;
			}
			
			while (num_brace != 0)
			{
				row++;
				
				if (rowStr[row].contains("{"))
				{
					num_brace++;
				}
				if (rowStr[row].contains("}"))
				{
					num_brace--;
				}
			}
			
			return succ;
		}
	}
	
	/**
	 * @param p 产生式字符串
	 * @return String type
	 * 执行for循环语句，计算迭代次数并执行内部语句
	 */
	public StringBuffer forLoop()
	{
		StringBuffer succ = new StringBuffer();
		String subs = rowStr[row].substring(rowStr[row].indexOf("(") + 1, rowStr[row].indexOf(")"));
		String[] str = subs.split(":");
		int max = Integer.parseInt(str[1]);
		int value = Integer.parseInt(str[0]);
		int beginFor = ++row;
		int endFor = ++row;
		
		while (value <= max)
		{
			while (row < rowStr.length && !rowStr[row].contains("}"))
			{
				rowStr[row] = runFunction(rowStr[row]);
				
				if (rowStr[row].contains("if"))
				{
					succ.append(ifStr());
				}
				else if (rowStr[row].contains("else"))
				{
					succ.append(elseStr());
				}
				else if (rowStr[row].contains("for"))
				{
					succ.append(forLoop());
				}
				else
				{
					succ.append(rowStr[row]);
				}
				
				row++;
			}
			
			endFor = row;
			row = beginFor;
			value++;
		}
		
		row = endFor;
		
		return succ;
	}
	
	/**
	 * @param s 产生式字符串
	 * @return String type
	 * 计算产生式中每个括号内的算术表达式
	 */
	public String calculateStr(String s)
	{
		int n = 0;
		String sub;
		String[] subs;
		String results = "";
		
		while (n < s.length())
		{
			if (s.charAt(n) == '(')
			{
				int num_parenthese = 1;
				int index_parenthese = n + 1;
				
				while (index_parenthese < s.length() && num_parenthese != 0)
				{
					if (s.charAt(index_parenthese) == '(')
					{
						num_parenthese++;
					}
					else if (s.charAt(index_parenthese) == ')')
					{
						num_parenthese--;
					}
					
					index_parenthese++;
				}
				
				sub = s.substring(n + 1, index_parenthese - 1);
				subs = sub.split(",");
				
				for (int i = 0; i < subs.length; i++)
				{
					if (!isNumeric(subs[i]))
					{
						results += calculate(subs[i]) + ",";
					}
					else
					{
						results += subs[i] + ",";
					}
				}
				
				results = results.substring(0, results.length() - 1);
				
				s = s.replace(s.substring(n + 1, index_parenthese - 1), results);
			}
			
			sub = null;
			subs = null;
			results = "";
			n++;
		}
		
		return s;
	}
	
	/**
	 * @param s 括号内的字符串表达式
	 * @return String type
	 * 计算字符串表达式
	 */
	public String calculate(String s)
	{
		Object o = new Object();
		
		s = s.replace("--", "+");
		s = s.replace("+-", "-");
		
		try
		{
			o = new ScriptEngineManager().getEngineByName("JavaScript").eval(s);
		}
		catch (ScriptException e)
		{
			System.out.println("Expression error!");
			System.out.println(s);
		}
		
		return String.valueOf(o);
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
	
	/**
	 * @param cond if条件语句字符串
	 * @return boolean type
	 * 执行if，else if条件语句判断是否满足条件
	 */
	public boolean ifCondition(String cond)
	{
		if (cond.equals("true"))
		{
			return true;
		}
		else if(cond.contains("PROBABILITY"))
		{
			double p_def;
			p_def = Double.parseDouble(cond.substring(cond.indexOf("(") + 1, cond.indexOf(")")));
			pro += p_def;
			
			if (p_ran < pro)
			{
				return true;
			}
			else
			{
				return false;
			}
		}
		else
		{
			return Boolean.valueOf(calculate(cond));
		}
	}
	
	public String runFunction(String row)
	{
		while (row.contains("Random"))
		{
			int index_start = row.indexOf("Random");
			int index_end = row.indexOf(")", index_start) + 1;
			
			String paraStr = row.substring(index_start + 7, index_end - 1);
			String[] params = paraStr.split(",");
			
			float pA = Float.parseFloat(calculate(params[0]));
			float pB = Float.parseFloat(calculate(params[1]));
			
			float value = (float) Math.random() * (pB - pA) + pA;
			
			row = row.replaceFirst("Random\\([^A-Za-z\\)]*\\)", "" + value);
		}
		
		return row;
	}
	
	public String getLString()
	{
		return w;
	}
}
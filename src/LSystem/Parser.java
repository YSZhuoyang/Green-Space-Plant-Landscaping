package LSystem;

import java.util.*;
import javax.script.*;


/**
 * @author Yu Sangzhuoyang
 * @version 1.15
 */
public class Parser
{
	private int row;									//����
	private double pro;									//�������ʽ�ĸ��ʵ���
	private double p_ran;								//�������Ϊ�����Ǹ���

	private boolean undone;								//��ʾif������Ƿ���ִ�й�ĳ�������µ����
	
	private String w;                           	    //����
	private String[] rowStr;							//��ű����ֵ�ÿһ��
	
	private Object[] key;
	
	private LinkedHashMap<String, String> production_hm;//�洢����ʽ
	
	/**
	 * @return none
	 * ���캯����������ʼ��
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
	 * �洢����ģ�飬����ǰ���ַ���������ʽ
	 */
	public void setUp(String axiom, LinkedHashMap<String,String> productions)
	{
		w = axiom;
		production_hm = productions;
		key = production_hm.keySet().toArray();
	}
	
	/**
	 * �����ַ��������滻������ǰ���ַ�����������Ӧ����ʽ��������ʽ������Ӧ������滻��
	 * �滻ǰ���ַ����е��ַ���
	 * @param none
	 * @return none
	 */
	public void parse()
	{
		int strIndex = 0;									//�����еĵ����ַ����±��
		
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
	 * @param p ����ʽ�ַ���
	 * @return String type
	 * ���������������ʽ����ִ��if�����������ж�
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
	 * @param p ����ʽ�ַ���
	 * @return String type
	 * ִ��if��������ж��Ƿ�������������������ִ���ڲ����
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
	 * @param p ����ʽ�ַ���
	 * @return String type
	 * ִ��else��������ж��Ƿ�������������������ִ���ڲ����
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
	 * @param p ����ʽ�ַ���
	 * @return String type
	 * ִ��forѭ����䣬�������������ִ���ڲ����
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
	 * @param s ����ʽ�ַ���
	 * @return String type
	 * �������ʽ��ÿ�������ڵ��������ʽ
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
	 * @param s �����ڵ��ַ������ʽ
	 * @return String type
	 * �����ַ������ʽ
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
	 * @param cond if��������ַ���
	 * @return boolean type
	 * ִ��if��else if��������ж��Ƿ���������
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
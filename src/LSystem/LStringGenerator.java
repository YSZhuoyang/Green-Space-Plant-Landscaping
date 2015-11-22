package LSystem;


/**
 * @author Yu Sangzhuoyang
 * @version 1.15
 */
public class LStringGenerator
{
	private String filePath;
	//private String log;			//Records
	
	private LexicalAnalyzer la;
	
	public LStringGenerator()
	{
		filePath = "";
		//log = "";
		
		la = new LexicalAnalyzer();
	}
	
	public String generateLString(String plant)
	{
		filePath = "data/" + plant + ".txt";
		
		la.load(filePath);
		
		la.process();
		
		return la.getLString();
	}
}
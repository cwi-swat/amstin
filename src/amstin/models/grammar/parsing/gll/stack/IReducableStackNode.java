package amstin.models.grammar.parsing.gll.stack;

public interface IReducableStackNode{
	boolean reduce(char[] input);
	
	boolean reduce(char[] input, int location);
	
	int getLength();
}

package amstin.models.grammar.parsing.gll.result;

import amstin.models.ast.Tree;
import amstin.models.grammar.parsing.gll.Production;
import amstin.models.grammar.parsing.gll.result.struct.Link;
import amstin.models.grammar.parsing.gll.util.IndexedStack;

public class CharNode extends AbstractNode{
	private final char character;
	
	public CharNode(char character){
		super();
		
		this.character = character;
	}
	
	@Override
	public void addAlternative(Production production, Link children){
		throw new UnsupportedOperationException();
	}
	
	public boolean isEpsilon(){
		return false;
	}
	
	public boolean isRejected(){
		return false;
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("char(");
		sb.append(getNumericCharValue(character));
		sb.append(')');
		return sb.toString();
	}
	
	public Tree toTerm(IndexedStack<AbstractNode> stack, int depth){
//		IInteger characterValue = vf.integer(getNumericCharValue(character));
//		return vf.constructor(Factory.Tree_Char, characterValue);
		return null;
	}
	
	public static int getNumericCharValue(char character){
		return (character > 127) ? Character.getNumericValue(character) : ((int) character); // Just ignore the Unicode garbage when possible.
	}
	
	
	public String toString(IndexedStack<AbstractNode> stack, int depth){
		return toString();
	}
}

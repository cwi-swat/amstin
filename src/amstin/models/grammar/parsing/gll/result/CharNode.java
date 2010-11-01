package amstin.models.grammar.parsing.gll.result;

import amstin.models.grammar.parsing.gll.prods.Production;
import amstin.models.grammar.parsing.gll.result.struct.Link;
import amstin.models.grammar.parsing.gll.util.IndexedStack;
import amstin.models.parsetree.Lit;
import amstin.models.parsetree.Tree;

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
	
	public Tree toTree(IndexedStack<AbstractNode> stack, int depth){
//		IInteger characterValue = vf.integer(getNumericCharValue(character));
//		return vf.constructor(Factory.Tree_Char, characterValue);
		Lit lit = new Lit();
		lit.value = new Character(character).toString();
		return lit;
	}
	
	public static int getNumericCharValue(char character){
		return (character > 127) ? Character.getNumericValue(character) : ((int) character); // Just ignore the Unicode garbage when possible.
	}
	
	
	public String toString(IndexedStack<AbstractNode> stack, int depth){
		return toString();
	}

	@Override
	public String yield() {
		return new Character(character).toString();
	}
}

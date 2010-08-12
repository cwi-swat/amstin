package amstin.models.grammar.parsing.gll.result;

import amstin.models.ast.Lit;
import amstin.models.ast.Tree;
import amstin.models.grammar.parsing.gll.Production;
import amstin.models.grammar.parsing.gll.result.struct.Link;
import amstin.models.grammar.parsing.gll.util.IndexedStack;

public class LiteralNode extends AbstractNode{
	
	private final Production production;
	private final char[] content;
	
	public LiteralNode(Production production, char[] content){
		super();
		
		this.production = production;
		this.content = content;
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
	
	private void printCharacter(int character, StringBuilder sb){
		sb.append("char(");
		sb.append(character);
		sb.append(')');
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		
		sb.append("appl(");
		sb.append(production);
		sb.append(',');
		sb.append('[');
		printCharacter(CharNode.getNumericCharValue(content[0]), sb);
		for(int i = 1; i < content.length; i++){
			sb.append(',');
			printCharacter(CharNode.getNumericCharValue(content[i]), sb);
		}
		sb.append(']');
		sb.append(')');
		
		return sb.toString();
	}

	@Override
	public String toString(IndexedStack<AbstractNode> stack, int depth) {
		return toString();
	}

	@Override
	public Tree toTree(IndexedStack<AbstractNode> stack, int depth) {
		Lit lit = new Lit();
		lit.value = new String(content);
		return lit;
	}
	
	
}

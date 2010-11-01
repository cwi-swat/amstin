package amstin.models.grammar.parsing.gll.result;

import amstin.models.grammar.parsing.gll.prods.Production;
import amstin.models.grammar.parsing.gll.result.struct.Link;
import amstin.models.grammar.parsing.gll.util.IndexedStack;
import amstin.models.parsetree.Tree;

public class EpsilonNode extends AbstractNode{
	private final static String EPSILON_STRING = "empty()";
	
	public EpsilonNode(){
		super();
	}
	
	@Override
	public void addAlternative(Production production, Link children){
		throw new UnsupportedOperationException();
	}
	
	public boolean isEpsilon(){
		return true;
	}
	
	public boolean isRejected(){
		return false;
	}
	
	@Override
	public String toString(){
		return EPSILON_STRING;
	}
	
	@Override
	public String toString(IndexedStack<AbstractNode> stack, int depth) {
		return toString();
	}

	@Override
	public Tree toTree(IndexedStack<AbstractNode> stack, int depth) {
		throw new AssertionError("should not happen");
	}

	@Override
	public String yield() {
		return "";
	}
}

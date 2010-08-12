package amstin.models.grammar.parsing.gll.result;

import amstin.models.ast.Tree;
import amstin.models.grammar.parsing.gll.Production;
import amstin.models.grammar.parsing.gll.result.struct.Link;
import amstin.models.grammar.parsing.gll.util.IndexedStack;

public abstract class AbstractNode{
	
	public AbstractNode(){
		super();
	}
	
	public boolean isContainer(){
		return (this instanceof ContainerNode);
	}
	
	public abstract boolean isEpsilon();
	
	public abstract boolean isRejected();
	
	public abstract void addAlternative(Production production, Link children);
	
	public abstract String toString(IndexedStack<AbstractNode> stack, int depth);

	public abstract Tree toTree(IndexedStack<AbstractNode> stack, int depth);
}

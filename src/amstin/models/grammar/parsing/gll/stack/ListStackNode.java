package amstin.models.grammar.parsing.gll.stack;

import amstin.models.grammar.parsing.gll.IGLL;
import amstin.models.grammar.parsing.gll.Production;
import amstin.models.grammar.parsing.gll.result.AbstractNode;
import amstin.models.grammar.parsing.gll.result.ContainerNode;
import amstin.models.grammar.parsing.gll.result.struct.Link;
import amstin.models.grammar.parsing.gll.util.ArrayList;
import amstin.models.grammar.parsing.gll.util.LinearIntegerKeyedMap;

public final class ListStackNode extends AbstractStackNode implements IListStackNode{
	private final Production production;

	private final AbstractStackNode child;
	private final boolean isPlusList;
	
	private ContainerNode result;
	
	public ListStackNode(int id, Production production, AbstractStackNode child, boolean isPlusList){
		super(id);
		
		this.production = production;
		
		this.child = child;
		this.isPlusList = isPlusList;
	}
	
	private ListStackNode(ListStackNode original){
		super(original);
		
		production = original.production;

		child = original.child;
		isPlusList = original.isPlusList;
	}
	
	private ListStackNode(ListStackNode original, LinearIntegerKeyedMap<ArrayList<Link>> prefixes){
		super(original, prefixes);
		
		production = original.production;

		child = original.child;
		isPlusList = original.isPlusList;
	}
	
	public String getName(){
		throw new UnsupportedOperationException();
	}
	
	public boolean reduce(char[] input){
		throw new UnsupportedOperationException();
	}
	
	public boolean isClean(){
		return (result == null);
	}
	
	public AbstractStackNode getCleanCopy(){
		return new ListStackNode(this);
	}
	
	public AbstractStackNode getCleanCopyWithPrefix(){
		return new ListStackNode(this, prefixesMap);
	}
	
	public void setResultStore(ContainerNode resultStore){
		result = resultStore;
	}
	
	public ContainerNode getResultStore(){
		return result;
	}
	
	public int getLength(){
		throw new UnsupportedOperationException();
	}
	
	public AbstractStackNode[] getChildren(){
		AbstractStackNode listNode = child.getCleanCopy();
		
		listNode.addNext(listNode);
		listNode.addEdge(this);
		listNode.addPrefix(null, startLocation);
		listNode.setStartLocation(startLocation);
		listNode.setParentProduction(production);
		
		if(isPlusList){
			return new AbstractStackNode[]{listNode};
		}
		
		EpsilonStackNode empty = new EpsilonStackNode(IGLL.DEFAULT_LIST_EPSILON_ID);

		empty.addEdge(this);
		empty.setStartLocation(startLocation);
		empty.setParentProduction(production);
		
		return new AbstractStackNode[]{listNode, empty};
	}
	
	public AbstractNode getResult(){
		return result;
	}

	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append(production);
		sb.append(getId());
		sb.append('(');
		sb.append(startLocation);
		sb.append(',');
		sb.append('?');
		sb.append(')');
		
		return sb.toString();
	}
}

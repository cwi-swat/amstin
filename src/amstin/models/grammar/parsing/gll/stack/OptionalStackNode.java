package amstin.models.grammar.parsing.gll.stack;

import amstin.models.grammar.parsing.gll.IGLL;
import amstin.models.grammar.parsing.gll.Production;
import amstin.models.grammar.parsing.gll.result.AbstractNode;
import amstin.models.grammar.parsing.gll.result.ContainerNode;
import amstin.models.grammar.parsing.gll.result.struct.Link;
import amstin.models.grammar.parsing.gll.util.ArrayList;
import amstin.models.grammar.parsing.gll.util.LinearIntegerKeyedMap;

public final class OptionalStackNode extends AbstractStackNode implements IListStackNode{
	private final Production production;
	
	private final AbstractStackNode optional;
	
	private ContainerNode result;
	
	public OptionalStackNode(int id, Production production, AbstractStackNode optional){
		super(id);
		
		this.production = production;
		
		this.optional = optional;
	}
	
	private OptionalStackNode(OptionalStackNode original){
		super(original);
		
		production = original.production;
		
		optional = original.optional;
	}
	
	private OptionalStackNode(OptionalStackNode original, LinearIntegerKeyedMap<ArrayList<Link>> prefixes){
		super(original, prefixes);
		
		production = original.production;
		
		optional = original.optional;
	}
	
	public String getName(){
		throw new UnsupportedOperationException();
	}
	
	public int getLength(){
		throw new UnsupportedOperationException();
	}
	
	public boolean reduce(char[] input){
		throw new UnsupportedOperationException();
	}
	
	public boolean isClean(){
		return (result == null);
	}
	
	public AbstractStackNode getCleanCopy(){
		return new OptionalStackNode(this);
	}
	
	public AbstractStackNode getCleanCopyWithPrefix(){
		return new OptionalStackNode(this, prefixesMap);
	}
	
	public void setResultStore(ContainerNode resultStore){
		result = resultStore;
	}
	
	public ContainerNode getResultStore(){
		return result;
	}
	
	public AbstractStackNode[] getChildren(){
		AbstractStackNode child = optional.getCleanCopy();
		AbstractStackNode empty = new EpsilonStackNode(IGLL.DEFAULT_LIST_EPSILON_ID);
		child.addEdge(this);
		empty.addEdge(this);
		
		child.setStartLocation(startLocation);
		child.setParentProduction(production);
		empty.setStartLocation(startLocation);
		empty.setParentProduction(production);
		
		return new AbstractStackNode[]{child, empty};
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

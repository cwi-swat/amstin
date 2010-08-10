package amstin.models.grammar.parsing.gll.stack;

import amstin.models.grammar.parsing.gll.result.ContainerNode;
import amstin.models.grammar.parsing.gll.result.EpsilonNode;
import amstin.models.grammar.parsing.gll.result.AbstractNode;
import amstin.models.grammar.parsing.gll.result.struct.Link;
import amstin.models.grammar.parsing.gll.util.ArrayList;
import amstin.models.grammar.parsing.gll.util.LinearIntegerKeyedMap;

public final class EpsilonStackNode extends AbstractStackNode implements IReducableStackNode{
	private final static EpsilonNode result = new EpsilonNode();
	
	public EpsilonStackNode(int id){
		super(id);
	}
	
	private EpsilonStackNode(EpsilonStackNode original){
		super(original);
	}
	
	private EpsilonStackNode(EpsilonStackNode original, LinearIntegerKeyedMap<ArrayList<Link>> prefixes){
		super(original, prefixes);
	}
	
	public String getName(){
		throw new UnsupportedOperationException();
	}
	
	public boolean reduce(char[] input){
		return true;
	}
	
	public boolean reduce(char[] input, int location){
		return true;
	}
	
	public boolean isClean(){
		return true;
	}
	
	public AbstractStackNode getCleanCopy(){
		return new EpsilonStackNode(this);
	}
	
	public AbstractStackNode getCleanCopyWithPrefix(){
		return new EpsilonStackNode(this, prefixesMap);
	}
	
	public void setResultStore(ContainerNode resultStore){
		throw new UnsupportedOperationException();
	}
	
	public ContainerNode getResultStore(){
		throw new UnsupportedOperationException();
	}
	
	public int getLength(){
		return 0;
	}
	
	public AbstractStackNode[] getChildren(){
		throw new UnsupportedOperationException();
	}
	
	public AbstractNode getResult(){
		return result;
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append('(');
		sb.append(startLocation);
		sb.append(')');
		
		return sb.toString();
	}
}

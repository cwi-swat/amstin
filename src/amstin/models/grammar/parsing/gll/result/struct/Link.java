package amstin.models.grammar.parsing.gll.result.struct;

import amstin.models.grammar.parsing.gll.result.AbstractNode;
import amstin.models.grammar.parsing.gll.util.ArrayList;

public class Link{
	public final ArrayList<Link> prefixes;
	public final AbstractNode node;
	
	public Link(ArrayList<Link> prefixes, AbstractNode node){
		super();
		
		this.prefixes = prefixes;
		this.node = node;
	}
}

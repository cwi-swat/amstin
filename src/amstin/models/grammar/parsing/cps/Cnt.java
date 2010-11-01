package amstin.models.grammar.parsing.cps;

import amstin.models.parsetree.Tree;


public interface Cnt {
	public int apply(int result, Tree obj);
}


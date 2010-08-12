package amstin.models.grammar.parsing.cps;

import amstin.models.ast.Tree;


public interface Cnt {
	public void apply(int result, Tree obj);
}


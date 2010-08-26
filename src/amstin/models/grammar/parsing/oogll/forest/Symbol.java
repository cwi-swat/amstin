package amstin.models.grammar.parsing.oogll.forest;

import java.util.Iterator;
import java.util.Set;

import amstin.models.grammar.parsing.oogll.symbol.Regular;

public class Symbol extends Tree {

	private Set<Tree> alternatives;
	private amstin.models.grammar.parsing.oogll.symbol.Symbol symbol;

	public Symbol(amstin.models.grammar.parsing.oogll.symbol.Symbol nt, Set<Tree> alternatives) {
		this.symbol = nt;
		this.alternatives = alternatives;
	}

	public Symbol(amstin.models.grammar.parsing.oogll.symbol.Symbol symbol) {
		this.symbol = symbol;
	}
	
	public void setAlternatives(Set<Tree> alternatives) {
		this.alternatives = alternatives;
	}

	@Override
	public String getLabel() {
		return symbol.toString();
	}

	@Override
	public String getShape() {
		if (alternatives.size() > 1) {
			return "diamond";
		}
		return "box";
	}
	
	@Override
	public Iterator<Tree> iterator() {
		return alternatives.iterator();
	}

	public amstin.models.grammar.parsing.oogll.symbol.Symbol getSymbol() {
		return symbol;
	}
	
}

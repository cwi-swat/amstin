package amstin.models.grammar.parsing.gll.prods;

import amstin.models.grammar.Iter;
import amstin.models.grammar.IterSep;
import amstin.models.grammar.IterSepStar;
import amstin.models.grammar.IterStar;
import amstin.models.grammar.Opt;
import amstin.models.grammar.Symbol;

public class Regular extends Production {
	private final Symbol symbol;

	public Regular(Symbol symbol) {
		this.symbol = symbol;
	}
	
	@Override
	public String toString() {
		if (symbol instanceof Opt) {
			return "opt";
		}
		if (symbol instanceof Iter) {
			return "iter";
		}
		if (symbol instanceof IterSep) {
			return "iter-sep";
		}
		if (symbol instanceof IterStar) {
			return "iter-star";
		}
		if (symbol instanceof IterSepStar) {
			return "iter-sep-star";
		}
		throw new IllegalArgumentException("Invalid regular symbol: " + symbol);
	}

}

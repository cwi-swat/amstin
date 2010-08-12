package amstin.models.grammar.parsing.gll.prods;

import amstin.models.grammar.Id;
import amstin.models.grammar.Int;
import amstin.models.grammar.Key;
import amstin.models.grammar.Lit;
import amstin.models.grammar.Real;
import amstin.models.grammar.Ref;
import amstin.models.grammar.Str;
import amstin.models.grammar.Symbol;

public class Terminal extends Production {

	private final Symbol symbol;

	public Terminal(Symbol symbol) {
		this.symbol = symbol;
	}
	
	@Override
	public String toString() {
		if (symbol instanceof Id) {
			return "id";
		}
		if (symbol instanceof Real) {
			return "real";
		}
		if (symbol instanceof Int) {
			return "int";
		}
		if (symbol instanceof Ref) {
			return "ref";
		}
		if (symbol instanceof Key) {
			return "key";
		}
		if (symbol instanceof Str) {
			return "str";
		}
		if (symbol instanceof Lit) {
			return "lit";
		}
		throw new IllegalArgumentException("Invalid terminal symbol: " + symbol);
	}
	
}

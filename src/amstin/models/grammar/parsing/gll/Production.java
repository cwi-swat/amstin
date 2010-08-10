package amstin.models.grammar.parsing.gll;

import amstin.models.grammar.Alt;
import amstin.models.grammar.Id;
import amstin.models.grammar.Int;
import amstin.models.grammar.Key;
import amstin.models.grammar.Lit;
import amstin.models.grammar.Real;
import amstin.models.grammar.Ref;
import amstin.models.grammar.Rule;
import amstin.models.grammar.Str;
import amstin.models.grammar.Symbol;

public class Production {
	
	private Alt alt;
	private Rule rule;
	private Symbol symbol;
	private String label;

	public Production(Rule rule, Alt alt) {
		this.rule = rule;
		this.alt = alt;
	}

	public Production(Symbol symbol) {
		this.symbol = symbol;
	}
	
	public Production(String string) {
		this.label = string;
	}

	public Object getKey() {
		if (rule != null) {
			return rule;
		}
		if (symbol != null) {
			return symbol;
		}
		return label;
	}

	@Override
	public String toString() {
		if (rule != null) {
			return rule.name;
		}
		if (symbol != null) {
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
			throw new IllegalArgumentException("Invalid symbol: " + symbol);
		}
		return label != null ? label : "<unknown>";
	}
}

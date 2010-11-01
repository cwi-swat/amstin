package amstin.models.grammar.parsing.gll.prods;

import amstin.models.grammar.Id;
import amstin.models.grammar.Int;
import amstin.models.grammar.Key;
import amstin.models.grammar.Lit;
import amstin.models.grammar.Real;
import amstin.models.grammar.Ref;
import amstin.models.grammar.Str;
import amstin.models.grammar.Symbol;
import amstin.models.parsetree.Tree;

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
	
	public Tree makeTree(String value) {
		if (symbol instanceof Id) {
			amstin.models.parsetree.Id x = new amstin.models.parsetree.Id();
			x.value = value;
			return x;
		}
		if (symbol instanceof Real) {
			amstin.models.parsetree.Real x = new amstin.models.parsetree.Real();
			x.value = Double.parseDouble(value);
			return x;
		}
		if (symbol instanceof Int) {
			amstin.models.parsetree.Int x = new amstin.models.parsetree.Int();
			x.value = Integer.parseInt(value);
			return x;
		}
		if (symbol instanceof Ref) {
			amstin.models.parsetree.Ref x = new amstin.models.parsetree.Ref();
			x.name = value;
			return x;
		}
		if (symbol instanceof Key) {
			amstin.models.parsetree.Def x = new amstin.models.parsetree.Def();
			x.name = value;
			return x;
		}
		if (symbol instanceof Str) {
			amstin.models.parsetree.Str x = new amstin.models.parsetree.Str();
			x.value = value.substring(1, value.length() - 1);
			return x;
		}
		throw new IllegalArgumentException("Invalx terminal symbol: " + symbol);
	}
	
}

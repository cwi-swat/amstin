package amstin.models.grammar.parsing.gll.prods;

import amstin.models.ast.Tree;
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
	
	public Tree makeTree(String value) {
		if (symbol instanceof Id) {
			amstin.models.ast.Id x = new amstin.models.ast.Id();
			x.value = value;
			return x;
		}
		if (symbol instanceof Real) {
			amstin.models.ast.Real x = new amstin.models.ast.Real();
			x.value = Double.parseDouble(value);
			return x;
		}
		if (symbol instanceof Int) {
			amstin.models.ast.Int x = new amstin.models.ast.Int();
			x.value = Integer.parseInt(value);
			return x;
		}
		if (symbol instanceof Ref) {
			amstin.models.ast.Ref x = new amstin.models.ast.Ref();
			x.name = value;
			return x;
		}
		if (symbol instanceof Key) {
			amstin.models.ast.Def x = new amstin.models.ast.Def();
			x.name = value;
			return x;
		}
		if (symbol instanceof Str) {
			amstin.models.ast.Str x = new amstin.models.ast.Str();
			x.value = value.substring(1, value.length() - 1);
			return x;
		}
		throw new IllegalArgumentException("Invalx terminal symbol: " + symbol);
	}
	
}

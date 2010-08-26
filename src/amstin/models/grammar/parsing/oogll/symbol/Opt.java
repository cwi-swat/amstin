package amstin.models.grammar.parsing.oogll.symbol;

import amstin.models.grammar.parsing.oogll.Alt;

public class Opt extends Regular {

	public Opt(Symbol symbol) {
		super(symbol, "?");
		addAlt(new Alt(symbol));
		addAlt(new Alt());
	}

}

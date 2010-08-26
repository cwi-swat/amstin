package amstin.models.grammar.parsing.oogll.symbol;

import amstin.models.grammar.parsing.oogll.GLL;

public interface Terminal extends Symbol {

	public String match(int pos, GLL driver);
	
}

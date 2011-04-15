package amstin.models.grammar.parsing.oogll.symbol;

import amstin.models.grammar.parsing.oogll.GLL;

public class Lit extends Base implements Terminal{

	private String literal;

	public Lit(String literal) {
		assert literal.length() > 0;
		this.literal = literal;
	}
	
	@Override
	public String match(int pos, GLL ctx) {
		if (ctx.getSrc().startsWith(literal, pos)) {
			return literal;
		}
		return null;
	}
	
	@Override
	public String toString() {
		return literal;
	}

	@Override
	public boolean isNullable() {
		return false;
	}

//	@Override
//	public boolean isInFirst(char c) {
//		return literal.charAt(0) == c;
//	}


}

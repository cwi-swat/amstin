package amstin.models.grammar.parsing.oogll.symbol;

import amstin.models.grammar.parsing.oogll.GLL;


public class Char extends Base implements Terminal {
	private final char ch;

	public Char(char ch) {
		this.ch = ch;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof Char)) {
			return false;
		}
		return ch == ((Char)obj).ch;
	}
	
	@Override
	public int hashCode() {
		return new Character(ch).hashCode();
	}
	
	@Override
	public String toString() {
		return "'" + ch + "'";
	}

	@Override
	public String match(int pos, GLL ctx) {
		if (ctx.getSrc().charAt(pos) == ch) {
			return ch + "";
		}
		return null;
	}

	@Override
	public boolean isNullable() {
		return false;
	}

//	@Override
//	public boolean isInFirst(char c) {
//		return this.ch == c;
//	}

	
}

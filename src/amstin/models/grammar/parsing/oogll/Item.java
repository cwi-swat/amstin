package amstin.models.grammar.parsing.oogll;

import java.util.Iterator;

import amstin.models.grammar.parsing.oogll.sppf.Node;
import amstin.models.grammar.parsing.oogll.symbol.NonTerminal;


public class Item implements IParser, Iterable<Item> {

	private final Alt alt;
	private final int dot;

	public Item(Alt alt, int dot) {
		this.alt = alt;
		this.dot = dot;
	}
	
	
	@Override
	public void parse(int pos, GSS cu, Node cn, GLL ctx) {
		alt.parseAt(dot, pos, cu, cn, ctx);
	}
	
	public int getDot() {
		return dot;
	}

	@Override
	public String toString() {
		String s = "";
		for (int i = 0; i < alt.arity(); i++) {
			if (i == dot) {
				s += " .";
			}
			s += " " + alt.get(i) ;
		}
		if (dot == alt.arity()) {
			s += " .";
		}
		return s;
	}
	
	@Override
	public boolean equals(Object obj) {
		return this == obj;
	}
	
	@Override
	public int hashCode() {
		return alt.hashCode() * 31 + dot * 7;
	}

	public boolean isAtEnd() {
		return dot == alt.arity();
	}

	public NonTerminal getNonTerminal() {
		return alt.getNonTerminal();
	}
	
	public int arity() {
		return alt.arity();
	}


	public Alt getAlt() {
		return alt;
	}


	// this iterator incrementally produces 
	@Override
	public Iterator<Item> iterator() {
		// TODO Auto-generated method stub
		return null;
	}


//	public boolean isInfirst(char c) {
//		if (alt.get(dot).isNullable()) {
//			if (dot < alt.arity() - 1) {
//				// if there's a follow-up symbol
//				return alt.getItem(dot + 1).isInfirst(c);
//			}
//			for (Item item: alt.getParents()) {
//				
//			}
//		}
//	}
	
}


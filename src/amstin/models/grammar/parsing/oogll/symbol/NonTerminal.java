package amstin.models.grammar.parsing.oogll.symbol;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import amstin.models.grammar.parsing.oogll.Alt;
import amstin.models.grammar.parsing.oogll.Item;

public class NonTerminal extends Base implements Iterable<Alt> {

	private final String name;
	private final List<Alt> alts = new ArrayList<Alt>();
	private int hashCode;
	
	public NonTerminal(String name) {
		this.name = name;
		this.hashCode = name.hashCode();
	}

	public void addAlt(Alt alt) {
		getAlts().add(alt);
		alt.setNonTerminal(this);
	}


	@Override
	public boolean equals(Object obj) {
		return this == obj;
	}
	
	@Override
	public int hashCode() {
		return hashCode;
	}
	
	@Override
	public String toString() {
		return getName();
	}

	@Override
	public Iterator<Alt> iterator() {
		return getAlts().iterator();
	}

	protected List<Alt> getAlts() {
		return alts;
	}

	public String getName() {
		return name;
	}

	private boolean nullableBusy = false;
	
	@Override
	public boolean isNullable() {
		if (nullableBusy) {
			return true;
		}
		nullableBusy = true;
		try {
			for (Alt a: this) {
				if (a.isNullable()) {
					return true;
				}
			}
		}
		finally {
			nullableBusy = false;
		}
		return false;
	}

//	private boolean firstBusy = false;
//	
//	@Override
//	public boolean isInFirst(char c) {
//		if (firstBusy) {
//			return true;
//		}
//		firstBusy = true;
//		try {
//			for (Alt a: this) {
//				if (a.isInFirst(c)) {
//					return true;
//				}
//			}
//		}
//		finally {
//			firstBusy = false;
//		}
//		return false;
//	}

//	private Set<Item> itemsUsingMe = new HashSet<Item>();
//	
//	public boolean isInFollow(char c) {
//		for (Item item: itemsUsingMe) {
//			if (item.nextItem().isInFirst(c)) {
//				return true;
//			}
//		}
//	}
//	
//	
//	@Override
//	public void addContext(Item item) {
//		contexts.add(item);
//	}
	
}

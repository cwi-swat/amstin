package amstin.models.grammar;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Grammar {

	public Rule startSymbol;
	public List<Rule> rules;
	
	public Set<String> reservedKeywords() {
		Set<String> lits = new HashSet<String>();
		lits.add("true");
		lits.add("false");
		for (Rule r: rules) {
			for (Alt a: r.alts) {
				for (Element e: a.elements) {
					if (e.symbol instanceof Lit) {
						lits.add(((Lit)e.symbol).value);
					}
				}
			}
		}
		return lits;
	}
}

package amstin.models.grammar.parsing.gll.prods;

import amstin.models.grammar.Alt;
import amstin.models.grammar.Rule;

public class Appl extends Production {

	private final Rule rule;
	private final Alt alt;

	public Appl(Rule rule, Alt alt) {
		this.rule = rule;
		this.alt = alt;
	}
	
	@Override
	public String toString() {
		if (alt.type != null) {
			return alt.type.name;
		}
		return rule.name;
	}
}

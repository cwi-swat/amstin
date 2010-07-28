package amstin.models.grammar;

import java.util.List;

public class Rule {

	public String name;
	public List<Alt> alts;
	
	public boolean isSingleton() {
		return alts.size() == 1;
	}
	
	
	public boolean isInjection() {
		if (isSingleton()) {
			return false;
		}
		for (Alt alt: alts) {
			if (alt.elements.size() != 1) {
				return false;
			}
			if (!(alt.elements.get(0).symbol instanceof Sym)) {
				return false;
			}
			if (alt.type != null) {
				return false;
			}
		}
		return true;
	}
}

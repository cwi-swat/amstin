package amstin.parsing.ast;

import java.util.HashMap;
import java.util.Map;

public class Symbol {
	private static final Map<String,Symbol> symbols = new HashMap<String, Symbol>();
	
	public static Symbol intern(String name) {
		if (!symbols.containsKey(name)) {
			symbols.put(name, new Symbol(name));
		}
		return symbols.get(name);
	}
	
	private String name;

	private Symbol(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		return name;
	}

	public String getName() {
		return name;
	}
}

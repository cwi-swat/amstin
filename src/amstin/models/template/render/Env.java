package amstin.models.template.render;

import java.util.HashMap;
import java.util.Map;

public class Env {
	
	private Map<String, Object> table;
	private Env parent;

	public Env() {
		this(null);
	}

	public Env(Env parent) {
		this.table = new HashMap<String, Object>();
		this.parent = parent;
	}
	
	public Object lookup(String name) {
		Env env = this;
		while (env != null && !env.table.containsKey(name)) {
			env = env.parent;
		}
		if (env == null) {
			return null;
		}
		return env.table.get(name);
	}
	
	public void store(String name, Object value) {
		table.put(name, value);
	}
	
}

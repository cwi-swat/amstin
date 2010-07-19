package amstin.tools;

import java.lang.reflect.Field;
import java.util.IdentityHashMap;
import java.util.List;

public class Labeling {

	public static IdentityHashMap<Object, Integer> label(Object obj) {
		return new Labeling(obj).label();
	}
	
	private IdentityHashMap<Object, Integer> visited;
	private int label;
	private Object root;

	private Labeling(Object root) {
		this.root = root;
		this.visited = new IdentityHashMap<Object, Integer>();
		this.label = 0;
	}

	private IdentityHashMap<Object, Integer> label() {
		labelRec(root);
		return visited;
	}


	@SuppressWarnings("unchecked")
	private void labelRec(Object o) {
		if (o == null) {
			return;
		}
		
		if (o instanceof String) {
			return;
		}
		
		if (o instanceof Boolean) {
			return;
		}
		
		if (o instanceof Integer) {
			return;
		}
		
		if (o instanceof Double) {
			return;
		}
		

		if (o instanceof List) {
			List l = (List)o;
			for (Object e: l) {
				labelRec(e);
			}
			return;
		}
		
		if (visited.containsKey(o)) {
			return;
		}
		visited.put(o, label++);
		
		
		Class<?> klass = o.getClass();
		for (Field f: klass.getFields()) {
			try {
				Object kid = f.get(o);
				labelRec(kid);
			} catch (IllegalArgumentException e) {
				throw new AssertionError(e);
			} catch (IllegalAccessException e) {
				throw new AssertionError(e);
			}
		}
	}
	
	
}

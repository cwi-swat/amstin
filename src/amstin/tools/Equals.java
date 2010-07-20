package amstin.tools;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.List;

@SuppressWarnings("unchecked")
public class Equals {

	private static final boolean DEBUG = false;

	public static boolean equals(Object o1, Object o2) {
		Equals eq = new Equals(o1, o2);
		return eq.equals();
	}


	private IdentityHashMap<Object, Object> done;
	private Object obj1;
	private Object obj2;

	private Equals(Object o1, Object o2) {
		this.obj1 = o1;
		this.obj2 = o2;
		this.done = new IdentityHashMap<Object, Object>();
	}


	private boolean equals() {
		return equalsRec(0, obj1, obj2);
	}

	private static void debug(int n, String s) {
		if (DEBUG) {
			for (int i = 0; i < n; i++) { 
				System.out.print(" ");
			}
			System.out.println(s);
		}
	}

	private boolean equalsRec(int n, Object o1, Object o2) {
		debug(n, "Checking EQUALS: o1 = " + o1 + "; o2 = " + o2);
		if (o1 == null && o2 != null) {
			debug(n, "o1 is null, o2 isn't");
			return false;
		}
		
		if (o1 != null && o2 == null) {
			debug(n, "o2 is null, o1 isn't");
			return false;
		}
		
		if (o1 == o2) {
			debug(n, "o1 == o2");
			return true;
		}
		
		if (o1.getClass() != o2.getClass()) {
			debug(n, "o1 and o2 have different classes");
			return false;
		}
		
		if (done.get(o1) == o2) {
			debug(n, "we've already compared o1 and o2");
			return true;
		}
		
		done.put(o1, o2);
		
		if (o1 instanceof List && o2 instanceof List) {
			List l1 = (List)o1;
			List l2 = (List)o2;
			if (l1.size() != l2.size()) {
				debug(n, "lists o1 and o2 are of unequal length");
				debug(n, "o1.size = " + l1.size() + "; o2.size = " + l2.size());
				
				if (DEBUG) {
					displayDiff(n, l1, l2);
				}
				return false;
			}
			for (int i = 0; i < l1.size(); i++) {
				if (!equalsRec(n + 1, l1.get(i), l2.get(i))) {
					debug(n, "element " + i + " of o1 and o2 differs");
					return false;
				}
			}
		}
		
		if (o1 instanceof String) {
			debug(n, "comparing string values");
			return o1.equals(o2);
		}
		else if (o1 instanceof Integer) {
			debug(n, "comparing integer values");
			return o1.equals(o2);
		}
		else if (o1 instanceof Boolean) {
			debug(n, "comparing boolean values");
			return o1.equals(o2);
		}
		else if (o1 instanceof Double) {
			debug(n, "comparing double values");
			return o1.equals(o2);
		}
		
		
		Class<?> klass = o1.getClass();
		debug(n, "comparing fields of o1 and o2");
		for (Field f: klass.getFields()) {
			try {
				debug(n, "comparing field: " + f.getName() + " in " + klass);
				Object kid1 = f.get(o1);
				Object kid2 = f.get(o2);
				if (!equalsRec(n + 1, kid1, kid2)) {
					debug(n, "field " + f.getName() + " differs");
					return false;
				}
				
			} catch (IllegalArgumentException e) {
				throw new AssertionError(e);
			} catch (IllegalAccessException e) {
				throw new AssertionError(e);
			}
		}
		return true;
		
	}


	private static void displayDiff(int n, List l1, List l2) {
		String os1[] = new String[l1.size()];
		String os2[] = new String[l2.size()];

		for (int i = 0; i < os1.length; i++) {
			os1[i] = l1.get(i).toString();
		}
		for (int i = 0; i < os2.length; i++) {
			os2[i] = l2.get(i).toString();
		}

		Arrays.sort(os1);
		Arrays.sort(os2);


		for (int i = 0; i < Math.max(l1.size(), l2.size()); i++) {
			Object k1 = i < l1.size() ? os1[i] : "n.a.";
			Object k2 = i < l2.size() ? os2[i] : "n.a.";
			debug(n, "o1[" + i + "] = " + k1 + "; o2[] = " + k2);
		}
	}


}

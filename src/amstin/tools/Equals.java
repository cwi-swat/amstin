package amstin.tools;

import java.lang.reflect.Field;
import java.util.IdentityHashMap;
import java.util.List;

public class Equals {

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
		return equalsRec(obj1, obj2);
	}


	@SuppressWarnings("unchecked")
	private boolean equalsRec(Object o1, Object o2) {
		if (o1 == null && o2 != null) {
			return false;
		}
		
		if (o1 != null && o2 == null) {
			return false;
		}
		
		if (o1 == o2) {
			return true;
		}
		
		if (o1.getClass() != o2.getClass()) {
			return false;
		}
		
		if (done.get(o1) == o2) {
			return true;
		}
		
		done.put(o1, o2);
		
		if (o1 instanceof List && o2 instanceof List) {
			List l1 = (List)o1;
			List l2 = (List)o2;
			if (l1.size() != l2.size()) {
				return false;
			}
			for (int i = 0; i < l1.size(); i++) {
				if (!equalsRec(l1.get(i), l2.get(i))) {
					return false;
				}
			}
		}
		
		if (o1 instanceof String) {
			return o1.equals(o2);
		}
		else if (o1 instanceof Integer) {
			return o1.equals(o2);
		}
		else if (o1 instanceof Boolean) {
			return o1.equals(o2);
		}
		else if (o1 instanceof Double) {
			return o1.equals(o2);
		}
		
		
		Class<?> klass = o1.getClass();
//		System.out.println("Comparing " + o1 + " and " + o2);
		for (Field f: klass.getFields()) {
			try {
//				System.out.println("comparing field: " + f.getName() + " in " + klass);
				Object kid1 = f.get(o1);
				Object kid2 = f.get(o2);
				if (!equalsRec(kid1, kid2)) {
//					System.out.println("\treturning false");
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


}

package amstin.tools;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import amstin.models.grammar.parsing.ast.Define;
import amstin.models.grammar.parsing.ast.Field;
import amstin.models.grammar.parsing.ast.Instance;
import amstin.models.grammar.parsing.ast.Reference;
import amstin.models.grammar.parsing.ast.Str;
import amstin.models.grammar.parsing.ast.Symbol;



public class Instantiate  {

	private String pkg;
	private List<Fix> fixes;
	private Object root;
	private Stack<Map<Symbol,Object>> defs;

	public static Object instantiate(String pkg, Object root) {
		Instantiate toJava = new Instantiate(pkg, root);
		return toJava.toJava();
	}
	
	private Instantiate(String pkg, Object root) {
		this.pkg = pkg;
		this.fixes = new ArrayList<Fix>();
		this.root = root;
//		this.defs = new HashMap<Symbol, Object>();
		this.defs = new Stack<Map<Symbol,Object>>();
		this.defs.push(new HashMap<Symbol, Object>());
	}
	
	private Object toJava() {
		Object result = toJava(root);
		for (Fix fix: fixes) {
			fix.apply(defs.peek());
		}
		return result;
	}

	
	@SuppressWarnings("unchecked")
	private Object toJava(Object obj) {
		if (obj == null) {
			return null;
		}
		if (obj instanceof Instance) {
			return instanceToJava((Instance)obj);
		}
		if (obj instanceof String) {
			return obj;
		}
		if (obj instanceof Str) {
			return ((Str)obj).getValue();
		}
		if (obj instanceof Integer) {
			return obj;
		}
		if (obj instanceof Boolean) {
			return obj;
		}
		if (obj instanceof Double) {
			return obj;
		}
		if (obj instanceof Symbol) {
			return ((Symbol)obj).getName();
		}
		if (obj instanceof Reference) {
			throw new ReferenceFound((Reference) obj);
		}
		if (obj instanceof Define) {
			throw new DefineFound((Define)obj);
		}
		if (obj instanceof List) {
			List<Object> newList = new ArrayList<Object>();
			int i = 0;
			for (Object o: ((List)obj)) {
				try {
					newList.add(toJava(o));
				}
				catch (ReferenceFound r) {
					newList.add(null);
					recordListFix(newList, i, r.ref);
				}
				// Defines cannot occur in ordinary lists
				// only as kids of instances (in rules).
				i++;
			}
			return newList;
		}
		throw new AssertionError("invalid object in Instantiate: " + obj);
	}


	private void recordListFix(List<Object> list, int index, Reference ref) {
		fixes.add(new ListFix(list, index, ref));
	}

	private void recordFieldFix(Object target, String fieldName, Reference ref) {
		fixes.add(new FieldFix(target, fieldName, ref));
	}


	private Object instanceToJava(Instance obj) {
		Class<?> klazz;
		try {
			klazz = Class.forName(pkg + "." + obj.getType());
			Object javaObject = klazz.newInstance();
			
			defs.push(new HashMap<Symbol, Object>());

			Symbol mySymbol = null;
			String myKeyField = null;
			
			for (Object kid: obj.getArgs()) {
				if (kid instanceof Field) { 
					String fieldName = ((Field)kid).getName();
					Object value = ((Field)kid).getValue();
					try {

						Object fieldValue = toJava(value);
						klazz.getField(fieldName).set(javaObject, fieldValue);
					}
					catch (ReferenceFound r) {
						recordFieldFix(javaObject, fieldName, r.ref);
					}
					catch (DefineFound d) {
						mySymbol = d.def.getSymbol();
						myKeyField = fieldName;
					}
				}
				else {
					throw new IllegalArgumentException("kids of instances must be fields, not " + kid);
				}
			}
			
			Map<Symbol, Object> kids = defs.pop();
			if (mySymbol != null) {
				for (Map.Entry<Symbol, Object> e: kids.entrySet()) {
					defs.peek().put(Symbol.intern(mySymbol.getName() + "." + e.getKey().getName()), e.getValue());
				}
				defs.peek().put(mySymbol, javaObject);
				klazz.getField(myKeyField).set(javaObject, mySymbol.getName());
			}
			else {
				defs.peek().putAll(kids);
			}
			
			return javaObject;
		
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
		
	}


	private static abstract class Fix {
		public abstract void apply(Map<Symbol,Object> defines);
	}
	
	private static class ListFix extends Fix {
		private List<Object> list;
		private int index;
		private Reference ref;

		public ListFix(List<Object> list, int index, Reference ref) {
			this.list = list;
			this.index = index;
			this.ref = ref;
		}

		@Override
		public void apply(Map<Symbol, Object> defines) {
			list.set(index, defines.get(ref.getSymbol()));
		}
	}
	
	private static class FieldFix extends Fix {
		private Object target;
		private String field;
		private Reference ref;

		public FieldFix(Object target, String field, Reference ref) {
			this.target = target;
			this.field = field;
			this.ref = ref;
		}
		
		@Override
		public void apply(Map<Symbol,Object> defines) {
			Symbol symbol = ref.getSymbol();
			if (!defines.containsKey(symbol)) {
				throw new AssertionError("Symbol " + symbol + " is not defined");
			}				
			
			Class<?> klazz = target.getClass();
			try {
				// TODO: typecheck references here?
				klazz.getField(field).set(target, defines.get(symbol));
			} catch (IllegalArgumentException e) {
				throw new RuntimeException(e);
			} catch (SecurityException e) {
				throw new RuntimeException(e);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			} catch (NoSuchFieldException e) {
				throw new RuntimeException(e);
			}
		}
		
	}
	
	@SuppressWarnings("serial")
	private static class ReferenceFound extends RuntimeException {
		private Reference ref;

		public ReferenceFound(Reference ref) {
			this.ref = ref;
		}
	}
	
	@SuppressWarnings("serial")
	private static class DefineFound extends RuntimeException {
		private Define def;

		public DefineFound(Define def) {
			this.def = def;
		}
	}
}

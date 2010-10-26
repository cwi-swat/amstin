package amstin.tools;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import amstin.models.ast.Arg;
import amstin.models.ast.Bool;
import amstin.models.ast.Def;
import amstin.models.ast.Id;
import amstin.models.ast.Int;
import amstin.models.ast.Lit;
import amstin.models.ast.Obj;
import amstin.models.ast.ParseTree;
import amstin.models.ast.Real;
import amstin.models.ast.Ref;
import amstin.models.ast.Str;
import amstin.models.ast.Tree;
import amstin.models.ast.Ws;



public class ASTtoModel  {
	
	// TODO this tool should use the grammar, for instance
	// to know whether certain optional literals are absent
	// in order to convert them to booleans. It should therefore
	// follow the strategy of ModelToString.

	private String pkg;
	private List<Fix> fixes;
	private ParseTree root;
	private Stack<Map<String,Object>> defs;

	public static Object instantiate(String pkg, ParseTree root) {
		ASTtoModel toJava = new ASTtoModel(pkg, root);
		return toJava.toJava();
	}
	
	private ASTtoModel(String pkg, ParseTree root) {
		this.pkg = pkg;
		this.fixes = new ArrayList<Fix>();
		this.root = root;
		this.defs = new Stack<Map<String,Object>>();
		this.defs.push(new HashMap<String, Object>());
	}
	
	private Object toJava() {
		Object result = toJava(root);
		for (Fix fix: fixes) {
			fix.apply(defs.peek());
		}
		return result;
	}

	private Object toJava(ParseTree pt) {
		return toJava(((Arg)pt.top).value);
	}

	private Object toJava(Tree obj) {
		if (obj == null) {
			return null;
		}
		if (obj instanceof Obj) {
			return instanceToJava((Obj)obj);
		}
		if (obj instanceof Str) {
			return ((Str)obj).value;
		}
		if (obj instanceof Int) {
			return ((Int)obj).value;
		}
		if (obj instanceof Real) {
			return ((Real)obj).value;
		}
		if (obj instanceof Id) {
			return ((Id)obj).value;
		}
		if (obj instanceof Bool) {
			return ((Bool)obj).value;
		}
		if (obj instanceof Ref) {
			throw new ReferenceFound((Ref)obj);
		}
		if (obj instanceof Def) {
			throw new DefineFound((Def)obj);
		}
		if (obj instanceof amstin.models.ast.List) {
			List<Object> newList = new ArrayList<Object>();
			int i = 0;
			for (Tree o: ((amstin.models.ast.List)obj).elements) {
				if (o instanceof Lit || o instanceof Ws) {
					continue;
				}
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
		throw new AssertionError("invalid object in ast to model: " + obj);
	}


	private void recordListFix(List<Object> list, int index, Ref ref) {
		fixes.add(new ListFix(list, index, ref));
	}

	private void recordFieldFix(Object target, String fieldName, Ref ref) {
		fixes.add(new FieldFix(target, fieldName, ref));
	}


	private Object instanceToJava(Obj obj) {
		Class<?> klazz;
		try {
			klazz = Class.forName(pkg + "." + obj.name);
			Object javaObject = klazz.newInstance();
			
			defs.push(new HashMap<String, Object>());

			String mySymbol = null;
			String myKeyField = null;
			
			for (Object kid: obj.args) {
				if (kid instanceof Lit || kid instanceof Ws) {
					continue;
				}
				if (kid instanceof Arg) { 
					String fieldName = ((Arg)kid).name;
					Tree value = ((Arg)kid).value;
					try {
						Object fieldValue = toJava(value);
						klazz.getField(fieldName).set(javaObject, fieldValue);
					}
					catch (ReferenceFound r) {
						recordFieldFix(javaObject, fieldName, r.ref);
					}
					catch (DefineFound d) {
						mySymbol = d.def.name;
						myKeyField = fieldName;
					}
				}
				else {
					throw new IllegalArgumentException("kids of instances must be fields, not " + kid);
				}
			}
			
			Map<String, Object> kids = defs.pop();
			if (mySymbol != null) {
				for (Map.Entry<String, Object> e: kids.entrySet()) {
					defs.peek().put(mySymbol + "." + e.getKey(), e.getValue());
				}
				defs.peek().put(mySymbol, javaObject);
				klazz.getField(myKeyField).set(javaObject, mySymbol);
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
		public abstract void apply(Map<String,Object> defines);
	}
	
	private static class ListFix extends Fix {
		private List<Object> list;
		private int index;
		private Ref ref;

		public ListFix(List<Object> list, int index, Ref ref) {
			this.list = list;
			this.index = index;
			this.ref = ref;
		}

		@Override
		public void apply(Map<String, Object> defines) {
			list.set(index, defines.get(ref.name));
		}
	}
	
	private static class FieldFix extends Fix {
		private Object target;
		private String field;
		private Ref ref;

		public FieldFix(Object target, String field, Ref ref) {
			this.target = target;
			this.field = field;
			this.ref = ref;
		}
		
		@Override
		public void apply(Map<String,Object> defines) {
			String symbol = ref.name;
			if (!defines.containsKey(symbol)) {
				throw new RuntimeException("Symbol " + symbol + " is not defined");
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
		private Ref ref;

		public ReferenceFound(Ref ref) {
			this.ref = ref;
		}
	}
	
	@SuppressWarnings("serial")
	private static class DefineFound extends RuntimeException {
		private Def def;

		public DefineFound(Def def) {
			this.def = def;
		}
	}
}

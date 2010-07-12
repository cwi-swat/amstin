package amstin.tools;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.IdentityHashMap;
import java.util.List;

public class CreateScript {

	public static void script(String pkg, String name, Object obj, Writer output) {
		CreateScript inst = new CreateScript(pkg, name, obj, output);
		inst.script();
	}
	
	private Object root;
	private IdentityHashMap<Object, Integer> visited;
	private int objNum;
	private String pkg;
	private String name;
	private Writer output;
	
	private CreateScript(String pkg, String name, Object obj, Writer output) {
		this.pkg = pkg;
		this.name = name;
		this.root = obj;
		this.output = output;
		this.visited = new IdentityHashMap<Object,Integer>();
		this.objNum = 0;
	}
	
	private void script() {
		instantiateRec(root);
		try {
			header();
			constructorInvocations();
			assignFields();
			footer();
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
		
	}
	
	
	private void header() throws IOException {
		Calendar cal = Calendar.getInstance();
		DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		output.write("/* Generated at " + df.format(cal.getTime()) + "; edit at your own risk. */\n\n");
		output.write("package " + pkg + ";\n\n");
		output.write("import java.util.ArrayList;\n\n");
		output.write("@SuppressWarnings(\"unchecked\")\n");
		output.write("public class " + name + " {\n");
		output.write("public static final " + root.getClass().getName() + " instance;\n");
		output.write("static {\n");
	}

	private void footer() throws IOException {
		output.write("instance = " + varForObj(root) + ";\n");
		output.write("}\n");
		output.write("}\n");
	}

	private void constructorInvocations() throws IOException {
		for (Object o: visited.keySet()) {
			String type = o.getClass().getName();
			output.write(type + " " + varForObj(o) + " = new " + type + "();\n");
		}
	}
	
	private String objToString(Object elt) {
		if (elt == null) {
			return "null";
		}
		else if (elt instanceof String) {
			// TODO escaping
			return "\"" + elt + "\"";
		}
		else if (elt instanceof Integer) {
			return elt.toString();
		}
		else if (elt instanceof Boolean) {
			return elt.toString();
		}
		else if (elt instanceof Double) {
			return elt.toString();
		}
		else if (visited.containsKey(elt)) {
			return varForObj(elt);
		}
		else {
			throw new IllegalArgumentException("cannot deal with " + elt);
		}
	}
	
	private String varForObj(Object obj) {
		return "obj" + visited.get(obj);
	}
	
	@SuppressWarnings("unchecked")
	private void assignFields() throws IOException {
		for (Object o: visited.keySet()) {
			Class<?> klass = o.getClass();
			String var = varForObj(o);
			for (Field f: klass.getFields()) {
				try {
					Object kid = f.get(o);
					
					String field = var + "." + f.getName();
					if (kid instanceof List) {
						if (((List)kid).isEmpty()) {
							output.write(field + " = new ArrayList();\n");
						}
						else {
							output.write(field + " = new ArrayList<" + ((List)kid).get(0).getClass().getName() + ">();\n");
						}
						for (Object elt: (List)kid) {
							output.write(field + ".add(" + objToString(elt) + ");\n");
						}
					}
					else {
						output.write(field + " = " + objToString(kid) + ";\n");
					}
				} catch (IllegalArgumentException e) {
					throw new AssertionError(e);
				} catch (IllegalAccessException e) {
					throw new AssertionError(e);
				}
			}
		}
	}

	
	@SuppressWarnings("unchecked")
	private void instantiateRec(Object o) {
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
				instantiateRec(e);
			}
			return;
		}
		
		if (visited.containsKey(o)) {
			return;
		}
		visited.put(o, objNum++);
		
		
		Class<?> klass = o.getClass();
		for (Field f: klass.getFields()) {
			try {
				Object kid = f.get(o);
				instantiateRec(kid);
			} catch (IllegalArgumentException e) {
				throw new AssertionError(e);
			} catch (IllegalAccessException e) {
				throw new AssertionError(e);
			}
		}
	}
	
}

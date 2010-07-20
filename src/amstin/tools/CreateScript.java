package amstin.tools;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.IdentityHashMap;
import java.util.List;

import amstin.tools.utils.Labeling;

public class CreateScript {

	// TODO: use code model here?
	
	public static void script(String pkg, String name, Object obj, Writer output) {
		CreateScript inst = new CreateScript(pkg, name, obj, output);
		inst.script();
	}
	
	private Object root;
	private IdentityHashMap<Object, Integer> labels;
	private String pkg;
	private String name;
	private Writer output;
	
	private CreateScript(String pkg, String name, Object obj, Writer output) {
		this.pkg = pkg;
		this.name = name;
		this.root = obj;
		this.output = output;
		this.labels = Labeling.label(root);
	}
	
	private void script() {
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
		for (Object o: labels.keySet()) {
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
		else if (labels.containsKey(elt)) {
			return varForObj(elt);
		}
		else {
			throw new IllegalArgumentException("cannot deal with " + elt);
		}
	}
	
	private String varForObj(Object obj) {
		return "obj" + labels.get(obj);
	}
	
	@SuppressWarnings("unchecked")
	private void assignFields() throws IOException {
		for (Object o: labels.keySet()) {
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
	
}

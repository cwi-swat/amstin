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

public class ModelToJavaScript {

	// TODO: use code model here?
	
	public static void script(String pkg, String name, Object obj, Writer output) {
		ModelToJavaScript inst = new ModelToJavaScript(pkg, name, obj, output);
		inst.script();
	}
	
	private Object root;
	private IdentityHashMap<Object, Integer> labels;
	private String pkg;
	private String name;
	private Writer output;
	
	private ModelToJavaScript(String pkg, String name, Object obj, Writer output) {
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
		output.write("function MakeObj() {");
	}

	private void footer() throws IOException {
		output.write("return " + varForObj(root) + ";\n");
		output.write("}");
	}

	private void constructorInvocations() throws IOException {
		for (Object o: labels.keySet()) {
			String type = o.getClass().getSimpleName();
			output.write(varForObj(o) + " = MakeBaseObject(\"" + type + "\");\n");
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
						output.write(field + " = [");
						String sep = " ";
						for (Object elt: (List)kid) {
							output.write(sep);
							output.write( objToString(elt) );
							sep = ", ";
						}
						output.write("];\n");
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

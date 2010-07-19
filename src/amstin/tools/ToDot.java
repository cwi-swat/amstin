package amstin.tools;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.util.IdentityHashMap;
import java.util.List;

@SuppressWarnings("unchecked")
public class ToDot {

	public static void main(String[] args) throws IOException {
		Object o = amstin.models.meta.Boot.instance;
		PrintWriter w = new PrintWriter(System.out);
		toDot(o, w);
		w.flush();

		
		FileWriter f = new FileWriter(new File("meta.dot"));
		toDot(o, f);
		f.close();
		
		o = amstin.models.grammar.Boot.instance;
		f = new FileWriter(new File("grammar.dot"));
		toDot(o, f);
		f.close();
	}
	
	public static void toDot(Object obj, Writer output) {
		ToDot inst = new ToDot(obj, output);
		try {
			inst.todot();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private Object root;
	private Writer output;
	private IdentityHashMap<Object, Integer> labels;


	private ToDot(Object obj, Writer output) {
		this.root = obj;
		this.output = output;
		this.labels = Labeling.label(root);
	}

	private void todot() throws IOException {
		header();
		nodes();
		edges();
		footer();
	}
	
	private void edges() throws IOException {
		for (Object o: labels.keySet()) {
			edges(o);
		}
	}

	private void edges(Object o) throws IOException {
		Class<?> klass = o.getClass();
		for (Field f: klass.getFields()) {
			try {
				Object kid = f.get(o);
				String name = f.getName();
				
				// TODO: what if a List contains str/int/etc. ?
				if (kid instanceof String || kid instanceof Boolean || kid instanceof Integer || kid instanceof Double) {
					// do nothing
				}
				else if (kid instanceof List) {
					for (int i = 0; i < ((List)kid).size(); i++) {
						Object elt = ((List)kid).get(i);
						output.write(nodeId(o) + " -> " + nodeId(elt) + " [label=\"" + name + "@" + i + "\"]\n");
					}
				}
				else if (kid != null) {
					output.write(nodeId(o) + " -> " + nodeId(kid) + " [label=\"" + name + "\"]\n");
				}
			} catch (IllegalArgumentException e) {
				throw new AssertionError(e);
			} catch (IllegalAccessException e) {
				throw new AssertionError(e);
			}
		}
		
	}

	private void nodes() throws IOException {
		for (Object o: labels.keySet()) {
			node(o);
		}
	}

	private void node(Object o) throws IOException {
		output.write(nodeId(o) + " [label=\"" + makeLabel(o) + "\"]\n");
	}
	
	private String nodeId(Object o) {
		return "node_" + labels.get(o);
	}

	private String makeLabel(Object o) {
		Class<?> klass = o.getClass();
		String label = "";
		for (Field f: klass.getFields()) {
			try {
				Object kid = f.get(o);
				String name = f.getName();
				if (kid instanceof String || kid instanceof Boolean || kid instanceof Integer || kid instanceof Double) {
					label += "|" + name + " = " + dotEscape(kid.toString());
				}
			} catch (IllegalArgumentException e) {
				throw new AssertionError(e);
			} catch (IllegalAccessException e) {
				throw new AssertionError(e);
			}
		}
		label = "{" + labels.get(o) + ": " + klass.getSimpleName() + label + "}";
		return label;
	}

	private String dotEscape(String string) {
		return string
		.replaceAll("\\|", "\\|")
		.replaceAll("\\n", "\\n")
		.replaceAll("\\t", "\\t")
		.replaceAll("\\f", "\\f")
		.replaceAll("\\r", "\\r")
		.replaceAll("\\{", "\\{") // these two don't work???
		.replaceAll("\\}", "\\}")
		.replaceAll("<", "\\<")
		.replaceAll(">", "\\>");
	}

	private void footer() throws IOException {
		output.write("}\n");
	}

	private void header() throws IOException {
		output.write("digraph aGraph {\n");
		output.write("node [shape=Mrecord]\n");
	}




}

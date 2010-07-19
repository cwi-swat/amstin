package amstin.tools;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.util.IdentityHashMap;
import java.util.List;

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
	private IdentityHashMap<Object, Integer> visited;
	private int node;


	private ToDot(Object obj, Writer output) {
		this.root = obj;
		this.output = output;
		this.visited = new IdentityHashMap<Object,Integer>();
		this.node = 0;
	}

	private void todot() throws IOException {
		toDotRec(root);
		header();
		nodes();
		edges();
		footer();
	}
	
	private void edges() throws IOException {
		for (Object o: visited.keySet()) {
			edges(o);
		}
	}

	private void edges(Object o) throws IOException {
		Class<?> klass = o.getClass();
		for (Field f: klass.getFields()) {
			try {
				Object kid = f.get(o);
				String name = f.getName();
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
		for (Object o: visited.keySet()) {
			node(o);
		}
	}

	private void node(Object o) throws IOException {
		String label = makeLabel(o);
		output.write(nodeId(o) + " [label=\"" + makeLabel(o) + "\"]\n");
	}
	
	private String nodeId(Object o) {
		return "node_" + visited.get(o);
	}

	private String makeLabel(Object o) {
		Class<?> klass = o.getClass();
		String label = "";
		for (Field f: klass.getFields()) {
			try {
				Object kid = f.get(o);
				String name = f.getName();
				if (kid instanceof String || kid instanceof Boolean || kid instanceof Integer || kid instanceof Double) {
//					label += "<tr><td>" + name + "</td><td>" + kid.toString() + "</td></tr>\n";
					label += "|" + name + " = " + dotEscape(kid.toString());
				}
			} catch (IllegalArgumentException e) {
				throw new AssertionError(e);
			} catch (IllegalAccessException e) {
				throw new AssertionError(e);
			}
		}
		//label = "<table>\n<tr><td colspan=\"2\">" + visited.get(o) + ": " + klass.getSimpleName() + "</td></tr>\n" + label + "</table>\n";
		label = "{" + visited.get(o) + ": " + klass.getSimpleName() + label + "}";
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

	private void toDotRec(Object o) {
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
				toDotRec(e);
			}
			return;
		}

		if (visited.containsKey(o)) {
			return;
		}
		visited.put(o, node++);


		Class<?> klass = o.getClass();
		for (Field f: klass.getFields()) {
			try {
				Object kid = f.get(o);
				toDotRec(kid);
			} catch (IllegalArgumentException e) {
				throw new AssertionError(e);
			} catch (IllegalAccessException e) {
				throw new AssertionError(e);
			}
		}
		
	}




}

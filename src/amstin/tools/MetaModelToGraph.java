package amstin.tools;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import amstin.models.grammar.Grammar;
import amstin.models.grammar.parsing.cps.Parser;
import amstin.models.graph.EdgeStat;
import amstin.models.graph.Field;
import amstin.models.graph.Field2Field;
import amstin.models.graph.Graph;
import amstin.models.graph.Node2Node;
import amstin.models.graph.NodeStat;
import amstin.models.graph.Port;
import amstin.models.graph.Record;
import amstin.models.graph.Stat;
import amstin.models.meta.Bool;
import amstin.models.meta.Class;
import amstin.models.meta.Int;
import amstin.models.meta.Klass;
import amstin.models.meta.MetaModel;
import amstin.models.meta.Opt;
import amstin.models.meta.Real;
import amstin.models.meta.Star;
import amstin.models.meta.Str;

public class MetaModelToGraph {

	private static final String TYPE_PORT = "___type";
	private MetaModel metaModel;

	private MetaModelToGraph(MetaModel metaModel) {
		this.metaModel = metaModel;
	}
	
	public static void main(String[] args) {
		MetaModel mm = amstin.models.meta.Boot.instance;
		MetaModelToGraph toGraph = new MetaModelToGraph(mm);
		Graph g = toGraph.toGraph();
		PrintWriter writer = new PrintWriter(System.out);
		Grammar grammar =  Parser.parseGrammar(amstin.models.graph._Main.GRAPH_MDG);
		ModelToString.unparse(grammar, g, writer);
		writer.flush();
	}
	
	private Graph toGraph() {
		Graph graph = new Graph();
		graph.statements = new ArrayList<Stat>();
		
		
		Map<Class,Record> table = new HashMap<Class, Record>();
		for (Class klass: metaModel.classes) {
			addNode(klass, table, graph.statements);
		}
		
		for (Class klass: metaModel.classes) {
			addEdges(klass, table, graph.statements);
		}
		
		/*
		 *  record <className> {
    			type "<className>",
    			<fieldName_1> "<typeName_n> <fieldName_n>",
    			....
    			<fieldName_n> "<typeName_n> <fieldName_n>"
    	    }
    	    // for all klass typed fields
    	    <className>.<fieldName> --> <className>.type;
		 */
		return graph;
	}


	private void addEdges(Class klass, Map<Class, Record> table, List<Stat> statements) {
		Record me = table.get(klass);
		for (amstin.models.meta.Field field: klass.fields) {
			Field2Field edge = new Field2Field();
			if (field.type instanceof Klass) {
				Class ref = ((Klass)field.type).klass;
				Record refNode = table.get(ref);
				for (Field f: me.fields) {
					if (((Port)f).port.equals(field.name)) {
						edge.from = f;
					}
				}
				for (Field f: refNode.fields) {
					if (((Port)f).port.equals(TYPE_PORT)) {
						edge.to = f;
					}
				}
				EdgeStat stat = new EdgeStat();
				stat.edge = edge;
				statements.add(stat);
			}
		}
		if (klass.parent != null) {
			Node2Node edge = new Node2Node();
			edge.from = me;
			edge.to = table.get(klass.parent.type);
			EdgeStat stat = new EdgeStat();
			stat.edge = edge;
			statements.add(stat);
		}
	}


	private void addNode(Class klass, Map<Class, Record> table,  List<Stat> statements) {
		Record node = new Record();
		node.name = klass.name;
		node.fields = new ArrayList<Field>();
		Port type = new Port();
		type.label = klass.name;
		type.port = TYPE_PORT;
		node.fields.add(type);
		for (amstin.models.meta.Field field: klass.fields) {
			Port f = new Port();
			f.label = fieldLabel(field);
			f.port = field.name;
			node.fields.add(f);
		}
		table.put(klass, node);
		NodeStat stat = new NodeStat();
		stat.node = node;
		statements.add(stat);
	}


	private String fieldLabel(amstin.models.meta.Field field) {
		String label = "";
		if (field.type instanceof Int) {
			label += "int";
		}
		if (field.type instanceof Str) {
			label += "str";
		}
		if (field.type instanceof Real) {
			label += "real";
		}
		if (field.type instanceof Bool) {
			label += "bool";
		}
		if (field.type instanceof Klass) {
			label += ((Klass)field.type).klass.name;
		}
		if (field.mult instanceof Star) {
			label += "*";
		}
		if (field.mult instanceof Opt) {
			label += "?";
		}
		label += " " + field.name;
		return label;
	}
	
	
	
	
	
}

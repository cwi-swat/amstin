package amstin.models.graph;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import amstin.Config;
import amstin.models.ast.ParseTree;
import amstin.models.ast.Tree;
import amstin.models.grammar.Grammar;
import amstin.models.grammar.parsing.cps.Parser;
import amstin.models.meta.MetaModel;
import amstin.tools.ASTtoString;
import amstin.tools.GrammarToMetaModel;
import amstin.tools.MetaModelToJava;
import amstin.tools.ModelToDot;
import amstin.tools.ModelToString;

public class _Main {
	private static final String GRAPH_DIR = Config.PKG_DIR + "/models/graph";
	private static final String GRAPH_PKG = Config.PKG +".models.graph";
	public static final String GRAPH_MDG = GRAPH_DIR + "/graph.mdg";
	private static final String EXAMPLE_GRAPH = GRAPH_DIR + "/example.graph";

	public static void main(String[] args) throws IOException {
		Grammar graphGrammar = Parser.parseGrammar(GRAPH_MDG);
		System.out.println(graphGrammar);
		MetaModel graphMetaModel = GrammarToMetaModel.infer("Graph", graphGrammar);
		PrintWriter writer = new PrintWriter(System.out);
		
		Grammar metaGrammar = Parser.parseGrammar(amstin.models.meta._Main.METAMODEL_MDG);
		ModelToString.unparse(metaGrammar, graphMetaModel, writer);
		System.out.println();
		writer.flush();
		
		

		File file = new File(Config.ROOT);
		// Warning: if there is a commit inbetween merges, merge updates to wrong version.
		MetaModelToJava.metaModelToJava(file, GRAPH_PKG, graphMetaModel);
		
		
		String example = Parser.readPath(EXAMPLE_GRAPH);
		Parser graphParser = new Parser(graphGrammar);
		
		ParseTree tree = graphParser.parse(example);
		System.out.println("YIELD:\n" +ASTtoString.astToString(tree));
		Object exampleModel = graphParser.parse(GRAPH_PKG, example);
		ModelToString.unparse(graphGrammar, exampleModel, writer);
		writer.flush();
		
		
		FileWriter dot = new FileWriter(new File("graph.dot"));
		ModelToDot.toDot(exampleModel, dot);
		dot.close();
		
	}
}

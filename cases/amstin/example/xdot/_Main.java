package amstin.example.xdot;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import amstin.Config;
import amstin.models.grammar.Grammar;
import amstin.models.grammar.parsing.cps.Parser;
import amstin.models.meta.MetaModel;
import amstin.models.parsetree.ParseTree;
import amstin.models.parsetree.Tree;
import amstin.tools.ParseTreeToString;
import amstin.tools.GrammarToMetaModel;
import amstin.tools.MetaModelToJava;
import amstin.tools.ModelToDot;
import amstin.tools.ModelToString;

public class _Main {
	private static final String XDOT_DIR = Config.PKG_DIR + "/models/xdot";
	private static final String XDOT_PKG = Config.PKG +".models.xdot";
	private static final String XDOT_MDG = XDOT_DIR + "/xdot.mdg";
	private static final String EXAMPLE_XDOT = XDOT_DIR + "/example.xdot";

	public static void main(String[] args) throws IOException {
		Grammar xdotGrammar = Parser.parseGrammar(XDOT_MDG);
		System.out.println(xdotGrammar);
		MetaModel xdotMetaModel = GrammarToMetaModel.infer("Graph", xdotGrammar);
		PrintWriter writer = new PrintWriter(System.out);
		
		Grammar metaGrammar = Parser.parseGrammar(amstin.models.meta._Main.METAMODEL_MDG);
		ModelToString.unparse(metaGrammar, xdotMetaModel, writer);
		System.out.println();
		writer.flush();
		
		

		File file = new File(Config.ROOT);
		// Warning: if there is a commit inbetween merges, merge updates to wrong version.
		MetaModelToJava.metaModelToJava(file, XDOT_PKG, xdotMetaModel);
		
		
		String example = Parser.readPath(EXAMPLE_XDOT);
		Parser xdotParser = new Parser(xdotGrammar);
		
		ParseTree tree = xdotParser.parse(example);
		System.out.println("YIELD:\n" +ParseTreeToString.parseTreeToString(tree));
		Object exampleModel = xdotParser.parse(XDOT_PKG, example);
		ModelToString.unparse(xdotGrammar, exampleModel, writer);
		writer.flush();
		
		
		FileWriter dot = new FileWriter(new File("xdot.dot"));
		ModelToDot.toDot(exampleModel, dot);
		dot.close();
		
	}
}

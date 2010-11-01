package amstin.models.parsetree;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import amstin.Config;
import amstin.models.grammar.Grammar;
import amstin.models.grammar.parsing.cps.Parser;
import amstin.models.meta.MetaModel;
import amstin.tools.ParseTreeToString;
import amstin.tools.GrammarToMetaModel;
import amstin.tools.MetaModelToJava;
import amstin.tools.ModelToDot;
import amstin.tools.ModelToString;

public class _Main {

	private static final String PARSETREE_DIR = Config.PKG_DIR + "/models/parsetree";
	private static final String PARSETREE_PKG = Config.PKG +".models.parsetree";
	private static final String PARSETREE_MDG = PARSETREE_DIR + "/parsetree.mdg";
	private static final String EXAMPLE_PARSETREE = PARSETREE_DIR + "/example.parsetree";

	public static void main(String[] args) throws IOException {
		Grammar parsetreeGrammar = Parser.parseGrammar(PARSETREE_MDG);
		MetaModel parsetreeMetaModel = GrammarToMetaModel.infer("PARSETREE", parsetreeGrammar);
		PrintWriter writer = new PrintWriter(System.out);
		
		Grammar metaGrammar = Parser.parseGrammar(amstin.models.meta._Main.METAMODEL_MDG);
		ModelToString.unparse(metaGrammar, parsetreeMetaModel, writer);
		writer.flush();
		
		

		File file = new File(Config.ROOT);
		MetaModelToJava.metaModelToJava(file, PARSETREE_PKG, parsetreeMetaModel);
		
		
		String example = Parser.readPath(EXAMPLE_PARSETREE);
		Parser parsetreeParser = new Parser(parsetreeGrammar);
		
		ParseTree examplePT = parsetreeParser.parse(example);
		
		System.out.println("PARSETREE = " + ParseTreeToString.parseTreeToString(examplePT));
		
		ParseTree exampleModel = (ParseTree) parsetreeParser.parse(PARSETREE_PKG, example);

		System.out.println("PARSETREE2 = " + ParseTreeToString.parseTreeToString(exampleModel));

		System.out.println();
		System.out.println();
		
		ModelToString.unparse(parsetreeGrammar, exampleModel, writer);
		writer.flush();

		System.out.println();
		System.out.println();

		ModelToString.unparse(parsetreeGrammar, examplePT, writer);
		writer.flush();

		
		FileWriter dot = new FileWriter(new File("parsetree.dot"));
		ModelToDot.toDot(exampleModel, dot);
		dot.close();
	}
}

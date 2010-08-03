package amstin.models.ast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import amstin.Config;
import amstin.models.grammar.Grammar;
import amstin.models.grammar.parsing.Parser;
import amstin.models.meta.MetaModel;
import amstin.tools.GrammarToMetaModel;
import amstin.tools.MetaModelToJava;
import amstin.tools.ModelToDot;
import amstin.tools.ModelToString;

public class _Main {

	private static final String AST_DIR = Config.PKG_DIR + "/models/ast";
	private static final String AST_PKG = Config.PKG +".models.ast";
	private static final String AST_MDG = AST_DIR + "/ast.mdg";
	private static final String EXAMPLE_AST = AST_DIR + "/example.ast";

	public static void main(String[] args) throws IOException {
		Grammar astGrammar = Parser.parseGrammar(AST_MDG);
		System.out.println(astGrammar);
		MetaModel astMetaModel = GrammarToMetaModel.infer("AST", astGrammar);
		PrintWriter writer = new PrintWriter(System.out);
		
		Grammar metaGrammar = Parser.parseGrammar(amstin.models.meta._Main.METAMODEL_MDG);
		ModelToString.unparse(metaGrammar, astMetaModel, writer);
		writer.flush();
		
		

		File file = new File(Config.ROOT);
		
		MetaModelToJava.metaModelToJava(file, AST_PKG, astMetaModel);
		
		
		String example = Parser.readPath(EXAMPLE_AST);
		Parser astParser = new Parser(astGrammar);
		
		Object exampleModel = astParser.parse(AST_PKG, example);
		System.out.println(exampleModel);
		
		ModelToString.unparse(astGrammar, exampleModel, writer);
		writer.flush();
		
		
		FileWriter dot = new FileWriter(new File("ast.dot"));
		ModelToDot.toDot(exampleModel, dot);
		dot.close();
	}
}

package amstin.models.ast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import amstin.Config;
import amstin.models.grammar.Grammar;
import amstin.models.grammar.parsing.cps.Parser;
import amstin.models.meta.MetaModel;
import amstin.models.parsetree.ParseTree;
import amstin.models.parsetree.implode.ParseTreeToAST;
import amstin.tools.GrammarToMetaModel;
import amstin.tools.MetaModelToJava;
import amstin.tools.ModelToDot;
import amstin.tools.ModelToString;
import amstin.tools.ParseTreeToString;

public class _Main {
	private static final String AST_PKG = Config.PKG +".models.ast";
	private static final String AST_MDG = Config.PKG_DIR + "/models/ast/ast.mdg";
	private static final String EXAMPLE_AST = Config.PKG_DIR + "/models/ast/sample.ast";


	
	public static void main(String[] args) throws IOException {
		Grammar astGrammar = Parser.parseGrammar(AST_MDG);
		System.out.println(astGrammar);
		Parser astParser = new Parser(astGrammar);
		
		MetaModel ASTMetaModel = GrammarToMetaModel.infer("Module", astGrammar);
		PrintWriter writer = new PrintWriter(System.out);
		
		Grammar metaGrammar = Parser.parseGrammar(amstin.models.meta._Main.METAMODEL_MDG);
		ModelToString.unparse(metaGrammar, ASTMetaModel, writer);
		System.out.println();
		writer.flush();
		
		

		File file = new File(Config.ROOT);
		MetaModelToJava.metaModelToJava(file, AST_PKG, ASTMetaModel);
		
		
		String example = Parser.readPath(EXAMPLE_AST);
		
		ParseTree tree = astParser.parse(example);
		System.out.println("YIELD:\n" +ParseTreeToString.parseTreeToString(tree));
		
		AST implode = ParseTreeToAST.parseTreeToAST(tree);
		writer.write("Implode of  parsertree of example AST:\n");
		ModelToString.unparse(astGrammar, implode, writer);
		writer.write("\n\n");
		writer.flush();
		
		Object exampleModel = astParser.parse(AST_PKG, example);
		ModelToString.unparse(astGrammar, exampleModel, writer);
		writer.flush();
		
		
		FileWriter dot = new FileWriter(new File("ast.dot"));
		ModelToDot.toDot(exampleModel, dot);
		dot.close();
	}
}

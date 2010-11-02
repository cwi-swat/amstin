package amstin.example.oberon0;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import amstin.Config;
import amstin.models.ast.AST;
import amstin.models.format.Box;
import amstin.models.format.Format;
import amstin.models.format.tobox.ASTtoBox;
import amstin.models.format.totext.BoxToText;
import amstin.models.grammar.Grammar;
import amstin.models.grammar.parsing.cps.Parser;
import amstin.models.meta.MetaModel;
import amstin.models.parsetree.ParseTree;
import amstin.models.parsetree.implode.ParseTreeToAST;
import amstin.tools.ParseTreeToString;
import amstin.tools.GrammarToMetaModel;
import amstin.tools.MetaModelToJava;
import amstin.tools.ModelToDot;
import amstin.tools.ModelToString;

import static amstin.models.format._Main.*;

public class _Main {
	private static final String OBERON0_PKG = Config.PKG +".example.oberon0";
	private static final String OBERON0_MDG = Config.EXAMPLE_PKG_DIR + "/example/oberon0/oberon0.mdg";
	private static final String EXAMPLE_OBERON0 = Config.EXAMPLE_PKG_DIR + "/example/oberon0/sample.oberon0";
	private static final String OBERON0_FORMAT = Config.EXAMPLE_PKG_DIR + "/example/oberon0/oberon0.format";


	
	public static void main(String[] args) throws IOException {
		Grammar oberon0 = Parser.parseGrammar(OBERON0_MDG);
		System.out.println(oberon0);
		Parser oberon0Parser = new Parser(oberon0);
		
		MetaModel oberon0MetaModel = GrammarToMetaModel.infer("Module", oberon0);
		PrintWriter writer = new PrintWriter(System.out);
		
		Grammar metaGrammar = Parser.parseGrammar(amstin.models.meta._Main.METAMODEL_MDG);
		ModelToString.unparse(metaGrammar, oberon0MetaModel, writer);
		System.out.println();
		writer.flush();
		
		

		File file = new File(Config.EXAMPLE_ROOT);
		MetaModelToJava.metaModelToJava(file, OBERON0_PKG, oberon0MetaModel);
		
		
		String example = Parser.readPath(EXAMPLE_OBERON0);
		
		ParseTree sampleTree = oberon0Parser.parse(example);
		System.out.println("YIELD:\n" +ParseTreeToString.parseTreeToString(sampleTree));
		Object exampleModel = oberon0Parser.parse(OBERON0_PKG, example);
		ModelToString.unparse(oberon0, exampleModel, writer);
		writer.flush();
		
		
		FileWriter dot = new FileWriter(new File("sample.dot"));
		ModelToDot.toDot(exampleModel, dot);
		dot.close();
		
		
		
		
		Grammar formatGrammar = Parser.parseGrammar(FORMAT_MDG);
		Parser formatParser = new Parser(formatGrammar);
		String oberon0FormatSrc = Parser.readPath(OBERON0_FORMAT);

		writer.write("\n");
		writer.write("\n");
		writer.flush();

		Format oberon0Format = (Format) formatParser.parse(FORMAT_PKG, oberon0FormatSrc);
		System.out.println(oberon0Format);
		
		ModelToString.unparse(formatGrammar, oberon0Format, writer);
		writer.write("\n");
		writer.write("\n");
		
		AST ast = ParseTreeToAST.parseTreeToAST(sampleTree);
		
		Box box = ASTtoBox.astToBox(oberon0Format, ast);

		String pretty = BoxToText.boxToText(box);
		writer.println(pretty);
		writer.flush();

	}
}

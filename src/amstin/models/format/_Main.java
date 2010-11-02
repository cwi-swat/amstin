package amstin.models.format;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import amstin.Config;
import amstin.models.ast.AST;
import amstin.models.format.tobox.ASTtoBox;
import amstin.models.format.totext.BoxToText;
import amstin.models.grammar.Grammar;
import amstin.models.grammar.parsing.cps.Parser;
import amstin.models.meta.MetaModel;
import amstin.models.parsetree.ParseTree;
import amstin.models.parsetree.implode.ParseTreeToAST;
import amstin.tools.GrammarToMetaModel;
import amstin.tools.MetaModelToJava;
import amstin.tools.ModelToDot;
import amstin.tools.ModelToString;

public class _Main {

	public static final String FORMAT_DIR = Config.PKG_DIR + "/models/format";
	public static final String FORMAT_PKG = Config.PKG +".models.format";
	public static final String FORMAT_MDG = FORMAT_DIR + "/format.mdg";
	public static final String EXAMPLE_FORMAT = FORMAT_DIR + "/ast.format";

	public static void main(String[] args) throws IOException {
		Grammar formatGrammar = Parser.parseGrammar(FORMAT_MDG);
		System.out.println(formatGrammar);
		MetaModel boxMetaModel = GrammarToMetaModel.infer("Format", formatGrammar);
		PrintWriter writer = new PrintWriter(System.out);
		
		Grammar metaGrammar = Parser.parseGrammar(amstin.models.meta._Main.METAMODEL_MDG);
		ModelToString.unparse(metaGrammar, boxMetaModel, writer);
		writer.write("\n");
		writer.write("\n");

		writer.flush();
		
		

		File file = new File(Config.ROOT);
		
		MetaModelToJava.metaModelToJava(file, FORMAT_PKG, boxMetaModel);
		
		
		String example = Parser.readPath(EXAMPLE_FORMAT);
		Parser boxParser = new Parser(formatGrammar);
		
		Format astFormat = (Format) boxParser.parse(FORMAT_PKG, example);
		System.out.println(astFormat);
		
		ModelToString.unparse(formatGrammar, astFormat, writer);
		writer.write("\n");
		writer.write("\n");

		writer.flush();
		
		FileWriter dot = new FileWriter(new File("pretty.dot"));
		ModelToDot.toDot(astFormat, dot);
		dot.close();
		
		
		/// formatting
		ParseTree pt = amstin.models.ast._Main.astParser().parse(new File(amstin.models.ast._Main.EXAMPLE_AST));
		AST ast = ParseTreeToAST.parseTreeToAST(pt);
		writer.write("\n");
		writer.write("\n");

		ModelToString.unparse(amstin.models.ast._Main.astGrammar(), ast, writer);
		writer.write("\n");
		writer.write("\n");

		Box box = ASTtoBox.astToBox(astFormat, ast);
		ModelToString.unparse(formatGrammar, box, writer);
		
		writer.write("\n");
		writer.write("\n");

		
		writer.write(BoxToText.boxToText(box));
		writer.flush();
		/////////
		
	

	}
}

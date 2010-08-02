package amstin.models.box;

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

	private static final String BOX_DIR = Config.PKG_DIR + "/models/box";
	private static final String BOX_PKG = Config.PKG +".models.box";
	private static final String BOX_MDG = BOX_DIR + "/box.mdg";
	private static final String EXAMPLE_BOX = BOX_DIR + "/example.pp";

	public static void main(String[] args) throws IOException {
		Grammar boxGrammar = Parser.parseGrammar(BOX_MDG);
		System.out.println(boxGrammar);
		MetaModel boxMetaModel = GrammarToMetaModel.infer("Entity", boxGrammar);
		PrintWriter writer = new PrintWriter(System.out);
		
		Grammar metaGrammar = Parser.parseGrammar(amstin.models.meta._Main.METAMODEL_MDG);
		ModelToString.unparse(metaGrammar, boxMetaModel, writer);
		writer.flush();
		
		

		File file = new File(Config.ROOT);
		
		MetaModelToJava.metaModelToJava(file, BOX_PKG, boxMetaModel);
		
		
		String example = Parser.readPath(EXAMPLE_BOX);
		Parser boxParser = new Parser(boxGrammar);
		
		Object exampleModel = boxParser.parse(BOX_PKG, example);
		System.out.println(exampleModel);
		
		ModelToString.unparse(boxGrammar, exampleModel, writer);
		writer.flush();
		
		
		FileWriter dot = new FileWriter(new File("pretty.dot"));
		ModelToDot.toDot(exampleModel, dot);
		dot.close();
		
	}
}

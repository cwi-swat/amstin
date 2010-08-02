package amstin.models.template;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import amstin.Config;
import amstin.models.grammar.Grammar;
import amstin.models.grammar.parsing.Parser;
import amstin.models.meta.MetaModel;
import amstin.models.template.render.Env;
import amstin.tools.GrammarToMetaModel;
import amstin.tools.ModelToDot;
import amstin.tools.ModelToString;


public class _Main {
	private static final String TEMPLATE_DIR = Config.PKG_DIR + "/models/template";
	private static final String TEMPLATE_PKG = Config.PKG +".models.template";
	private static final String TEMPLATE_MDG = TEMPLATE_DIR + "/template.mdg";
	private static final String TEMPLATE_META = TEMPLATE_DIR + "/template.meta";
	private static final String EXAMPLE_TEMPLATE = TEMPLATE_DIR + "/example.template";

	public static void main(String[] args) throws IOException {
		Grammar templateGrammar = Parser.parseGrammar(TEMPLATE_MDG);
		System.out.println(templateGrammar);
		MetaModel templateMetaModel = GrammarToMetaModel.infer("Template", templateGrammar);
		PrintWriter writer = new PrintWriter(System.out);
		
		Grammar metaGrammar = Parser.parseGrammar(amstin.models.meta._Main.METAMODEL_MDG);
		ModelToString.unparse(metaGrammar, templateMetaModel, writer);
		writer.flush();
		
		

		File file = new File(Config.ROOT);
		
//		MetaModelToJava.metaModelToJava(file, TEMPLATE_PKG, templateMetaModel);
		
		
		String example = Parser.readPath(EXAMPLE_TEMPLATE);
		Parser templateParser = new Parser(templateGrammar);
		
		Definitions exampleModel = (Definitions) templateParser.parse(TEMPLATE_PKG, example);
		System.out.println("\nmodel = "  + exampleModel);
		
		writer.write("\n\n");
		exampleModel.eval(writer);
		writer.write("\n\n");
		
		ModelToString.unparse(templateGrammar, exampleModel, writer);
		writer.flush();
		
		
		FileWriter dot = new FileWriter(new File("layout.dot"));
		ModelToDot.toDot(exampleModel, dot);
		dot.close();
		
	}
	
}

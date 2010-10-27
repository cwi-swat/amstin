package amstin.example.schema;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import amstin.Config;
import amstin.models.grammar.Grammar;
import amstin.models.grammar.parsing.cps.Parser;
import amstin.models.meta.MetaModel;
import amstin.tools.GrammarToMetaModel;
import amstin.tools.MetaModelToJava;
import amstin.tools.ModelToDot;
import amstin.tools.ModelToString;

public class _Main {

	private static final String SCHEMA_DIR = Config.PKG_DIR + "/models/schema";
	private static final String SCHEMA_PKG = Config.PKG +".models.schema";
	public static final String SCHEMA_MDG = SCHEMA_DIR + "/schema.mdg";
	private static final String EXAMPLE_SCHEMA = SCHEMA_DIR + "/example.schema";

	public static void main(String[] args) throws IOException {
		Grammar schemaGrammar = Parser.parseGrammar(SCHEMA_MDG);
		System.out.println(schemaGrammar);
		MetaModel schemaMetaModel = GrammarToMetaModel.infer("Entity", schemaGrammar);
		PrintWriter writer = new PrintWriter(System.out);
		
		Grammar metaGrammar = Parser.parseGrammar(amstin.models.meta._Main.METAMODEL_MDG);
		ModelToString.unparse(metaGrammar, schemaMetaModel, writer);
		writer.flush();
		
		

		File file = new File(Config.ROOT);
		
		MetaModelToJava.metaModelToJava(file, SCHEMA_PKG, schemaMetaModel);
		
		
		String example = Parser.readPath(EXAMPLE_SCHEMA);
		Parser schemaParser = new Parser(schemaGrammar);
		
		Object exampleModel = schemaParser.parse(SCHEMA_PKG, example);
		System.out.println(exampleModel);
		
		ModelToString.unparse(schemaGrammar, exampleModel, writer);
		writer.flush();
		
		
		FileWriter dot = new FileWriter(new File("schema.dot"));
		ModelToDot.toDot(exampleModel, dot);
		dot.close();
		
	}
	
}

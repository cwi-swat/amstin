package amstin.example.entity;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import amstin.Config;
import amstin.models.grammar.Grammar;
import amstin.models.grammar.parsing.cps.Parser;
import amstin.models.meta.MetaModel;
import amstin.models.parsetree.ParseTree;
import amstin.tools.ParseTreeToString;
import amstin.tools.GrammarToMetaModel;
import amstin.tools.ModelToDot;
import amstin.tools.ModelToString;

public class _Main {
	
	private static final String ENTITY_DIR = Config.PKG_DIR + "/models/entity";
	private static final String ENTITY_PKG = Config.PKG +".models.entity";
	private static final String ENTITY_MDG = ENTITY_DIR + "/entity.mdg";
	private static final String EXAMPLE_ENTITY = ENTITY_DIR + "/petstore.entity";

	public static void main(String[] args) throws IOException {
		Grammar entityGrammar = Parser.parseGrammar(ENTITY_MDG);
		System.out.println(entityGrammar);
		MetaModel entityMetaModel = GrammarToMetaModel.infer("Entity", entityGrammar);
		PrintWriter writer = new PrintWriter(System.out);
		
		Grammar metaGrammar = Parser.parseGrammar(amstin.models.meta._Main.METAMODEL_MDG);
		ModelToString.unparse(metaGrammar, entityMetaModel, writer);
		System.out.println();
		writer.flush();
		
		

//		File file = new File(Config.ROOT);
		// Warning: if there is a commit inbetween merges, merge updates to wrong version.
//		MetaModelToJava.metaModelToJava(file, ENTITY_PKG, entityMetaModel);
		
		
		String example = Parser.readPath(EXAMPLE_ENTITY);
		Parser entityParser = new Parser(entityGrammar);
		
		ParseTree tree = entityParser.parse(example);
		System.out.println("YIELD:\n" +ParseTreeToString.parseTreeToString(tree));
		Object exampleModel = entityParser.parse(ENTITY_PKG, example);
		ModelToString.unparse(entityGrammar, exampleModel, writer);
		writer.flush();
		
		
		FileWriter dot = new FileWriter(new File("petstore.dot"));
		ModelToDot.toDot(exampleModel, dot);
		dot.close();
		
	}
	
	
}

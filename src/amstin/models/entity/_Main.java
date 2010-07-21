package amstin.models.entity;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import amstin.Config;
import amstin.models.grammar.Grammar;
import amstin.models.grammar.parsing.Parser;
import amstin.models.meta.MetaModel;
import amstin.tools.InferMetaModel;
import amstin.tools.MetaModelToJava;
import amstin.tools.ToDot;
import amstin.tools.Unparse;

public class _Main {
	
	private static final String ENTITY_DIR = Config.PKG_DIR + "/models/entity";
	private static final String ENTITY_PKG = Config.PKG +".models.entity";
	private static final String ENTITY_MDG = ENTITY_DIR + "/entity.mdg";
	private static final String ENTITY_META = ENTITY_DIR + "/entity.meta";
	private static final String EXAMPLE_ENTITY = ENTITY_DIR + "/example.entity";

	public static void main(String[] args) throws IOException {
		Grammar entityGrammar = Parser.parseGrammar(ENTITY_MDG);
		System.out.println(entityGrammar);
		MetaModel entityMetaModel = InferMetaModel.infer("Entity", entityGrammar);
		PrintWriter writer = new PrintWriter(System.out);
		
		Grammar metaGrammar = Parser.parseGrammar(amstin.models.meta._Main.METAMODEL_MDG);
		Unparse.unparse(metaGrammar, entityMetaModel, writer);
		writer.flush();
		
		

		File file = new File(Config.ROOT);
		
		MetaModelToJava.metaModelToJava(file, ENTITY_PKG, entityMetaModel);
		
		
		String example = Parser.readPath(EXAMPLE_ENTITY);
		Parser entityParser = new Parser(entityGrammar);
		
		Object exampleModel = entityParser.parse(ENTITY_PKG, example);
		System.out.println(exampleModel);
		
		Unparse.unparse(entityGrammar, exampleModel, writer);
		writer.flush();
		
		
		FileWriter dot = new FileWriter(new File("employee.dot"));
		ToDot.toDot(exampleModel, dot);
		dot.close();
		
	}
	
	
}

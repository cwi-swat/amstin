package amstin.models.bayes;

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


	private static final String BAYES_DIR = Config.PKG_DIR + "/models/bayes";
	private static final String BAYES_PKG = Config.PKG +".models.bayes";
	private static final String BAYES_MDG = BAYES_DIR + "/bayes.mdg";
	private static final String EXAMPLE_BAYES = BAYES_DIR + "/example.bayes";

	public static void main(String[] args) throws IOException {
		Grammar bayesGrammar = Parser.parseGrammar(BAYES_MDG);
		System.out.println(bayesGrammar);
		MetaModel bayesMetaModel = InferMetaModel.infer("Bayes", bayesGrammar);
		PrintWriter writer = new PrintWriter(System.out);
		
		Grammar metaGrammar = Parser.parseGrammar(amstin.models.meta._Main.METAMODEL_MDG);
		Unparse.unparse(metaGrammar, bayesMetaModel, writer);
		writer.flush();
		

		File file = new File(Config.ROOT);
		
		//MetaModelToJava.metaModelToJava(file, BAYES_PKG, bayesMetaModel);
		
		
		String example = Parser.readPath(EXAMPLE_BAYES);
		Parser bayesParser = new Parser(bayesGrammar);
		
		Object exampleModel = bayesParser.parse(BAYES_PKG, example);
		System.out.println(exampleModel);
		
		Unparse.unparse(bayesGrammar, exampleModel, writer);
		writer.flush();
		
		
		FileWriter dot = new FileWriter(new File("bayes.dot"));
		ToDot.toDot(exampleModel, dot);
		dot.close();
		
	}
}

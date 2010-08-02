package amstin.models.grammar;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import amstin.Config;
import amstin.models.grammar.parsing.Parser;
import amstin.models.meta.MetaModel;
import amstin.tools.CheckModel;
import amstin.tools.ModelToJava;
import amstin.tools.ModelEquality;
import amstin.tools.ModelToDot;
import amstin.tools.ModelToString;


public class _Main {

	public static final String GRAMMAR_MDG = Config.PKG_DIR + "/models/grammar/grammar.mdg";
	public static final String GRAMMAR_META = Config.PKG_DIR + "/models/grammar/grammar.meta";
	public static final String GRAMMAR_PKG = Config.PKG + ".models.grammar";


	public static void main(String[] args) throws IOException {
		Grammar itself0 = Boot.instance;
		String src = Parser.readPath(GRAMMAR_MDG);

		Parser parser0 = new Parser(itself0);
		Grammar itself1 = (Grammar) parser0.parse(GRAMMAR_PKG, src);
		
//		
		Parser parser1 = new Parser((Grammar) itself1);
		Grammar itself2 = (Grammar) parser1.parse(GRAMMAR_PKG, src);
		
		System.out.println("grammar0 eq grammar1? " + ModelEquality.equals(itself0, itself1));
		System.out.println("grammar1 eq grammar2? " + ModelEquality.equals(itself1, itself2));
		System.out.println("grammar0 eq grammar2? " + ModelEquality.equals(itself0, itself2));
		
		
		Grammar meta = Parser.parseGrammar(amstin.models.meta._Main.METAMODEL_MDG);
		String grammarMeta = Parser.readPath(GRAMMAR_META);
		Parser metaParser = new Parser(meta);
		MetaModel grammarMetaModel = (MetaModel)metaParser.parse(amstin.models.meta._Main.METAMODEL_PKG, grammarMeta);
		
		List<String> errors = CheckModel.checkInstance(grammarMetaModel, itself1);
		for (String error: errors) {
			System.out.println("ERROR: " + error);
		}
		if (errors.isEmpty()) {
			FileWriter f = new FileWriter(new File(Config.PKG_DIR + "/models/grammar/Boot.java"));
			ModelToJava.script(GRAMMAR_PKG, "Boot", itself1, f);
			f.flush();
		}
		
		PrintWriter writer = new PrintWriter(System.out);
		ModelToString.unparse(itself1, itself1, writer);
		writer.flush();
		
//		ToDot.toDot(itself0, writer);
//		writer.flush();
		
	}
	
}

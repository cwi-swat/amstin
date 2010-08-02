package amstin.models.meta;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import amstin.Config;
import amstin.models.grammar.Grammar;
import amstin.models.grammar.parsing.Parser;
import amstin.tools.CheckModel;
import amstin.tools.ModelToJava;
import amstin.tools.ModelEquality;
import amstin.tools.ModelToString;


public class _Main {

	private static final String BOOT_JAVA = Config.PKG_DIR + "/models/meta/Boot.java";
	
	public static final String METAMODEL_PKG = Config.PKG + ".models.meta";
	public static final String METAMODEL_MDG = Config.PKG_DIR + "/models/meta/metamodel.mdg";
	public static final String METAMODEL_META = Config.PKG_DIR + "/models/meta/metamodel.meta";

	public static void main(String[] args) throws IOException {
		Grammar meta = Parser.parseGrammar(METAMODEL_MDG);
		String metaMeta = Parser.readPath(METAMODEL_META);
		Parser metaParser = new Parser(meta);
		MetaModel metaMetaModel = (MetaModel)metaParser.parse(METAMODEL_PKG, metaMeta);

		System.out.println("parsed eq boot? " + ModelEquality.equals(metaMetaModel, Boot.instance));
		
		// Check if metaMetaModel conforms to itself
		List<String> errors = CheckModel.checkInstance(metaMetaModel, metaMetaModel);
		for (String error: errors) {
			System.out.println("ERROR: " + error);
		}

		if (errors.isEmpty()) {
			FileWriter out = new FileWriter(new File(BOOT_JAVA));
//			PrintWriter out = new PrintWriter(System.out);
			ModelToJava.script(METAMODEL_PKG, "Boot", metaMetaModel, out);
			out.flush();
		}
		
		PrintWriter writer = new PrintWriter(System.out);
		ModelToString.unparse(meta, metaMetaModel, writer);
		writer.flush();
		
	}
}

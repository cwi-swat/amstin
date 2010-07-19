package amstin.models.stm;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import amstin.Config;
import amstin.models.grammar.Grammar;
import amstin.models.meta.MetaModel;
import amstin.parsing.Parser;
import amstin.tools.CheckInstance;
import amstin.tools.CreateScript;
import amstin.tools.Instantiate;
import amstin.tools.ToDot;
import amstin.tools.Unparse;


public class _Main {
	private static final String STATEMACHINE_PKG = Config.PKG +".models.stm";
	private static final String STATEMACHINE_MDG = Config.PKG_DIR + "/models/stm/statemachine.mdg";
	private static final String STATEMACHINE_META = Config.PKG_DIR + "/models/stm/statemachine.meta";
	private static final String GRANT_STM = Config.PKG_DIR +  "/models/stm/grant.stm";

	
//	private static final String METAMODEL_MDG = Config.PKG_DIR + "/models/meta/metamodel.mdg";

	
	public static void main(String[] args) throws IOException {
		Grammar stm = Parser.parseGrammar(STATEMACHINE_MDG);
		System.out.println(stm);
		String grant = Parser.readPath(GRANT_STM);
		Parser stmParser = new Parser(stm);
		Object obj = stmParser.parse(grant);
		System.out.println(obj);
		StateMachine grantStm = (StateMachine) Instantiate.instantiate(STATEMACHINE_PKG, obj);
		System.out.println(grantStm);
		
		PrintWriter out = new PrintWriter(System.out);
		CreateScript.script("a2mtk.models.stm", "Grant", grantStm, out);
		out.flush();
		
		
		Grammar meta = Parser.parseGrammar(amstin.models.meta._Main.METAMODEL_MDG);
		String stmMeta = Parser.readPath(STATEMACHINE_META);
		Parser metaParser = new Parser(meta);
		MetaModel stmMetaModel = (MetaModel)metaParser.parse(amstin.models.meta._Main.METAMODEL_PKG, stmMeta);
		
		List<String> errors = CheckInstance.checkInstance(stmMetaModel, grantStm);
		for (String error: errors) {
			System.out.println("ERROR: " + error);
		}
		
		
		PrintWriter writer = new PrintWriter(System.out);
		Unparse.unparse(stm, grantStm, writer);
		writer.flush();
		
		FileWriter f = new FileWriter(new File("stm.dot"));
		ToDot.toDot(grantStm, f);
		f.flush();
		
	}
}

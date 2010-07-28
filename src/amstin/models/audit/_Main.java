package amstin.models.audit;

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
	
	private static final String AUDIT_DIR = Config.PKG_DIR + "/models/audit";
	private static final String AUDIT_PKG = Config.PKG +".models.audit";
	private static final String AUDIT_MDG = AUDIT_DIR + "/audit.mdg";
	private static final String EXAMPLE_AUDIT = AUDIT_DIR + "/example.audit";

	public static void main(String[] args) throws IOException {
		Grammar auditGrammar = Parser.parseGrammar(AUDIT_MDG);
		System.out.println(auditGrammar);
		MetaModel auditMetaModel = InferMetaModel.infer("Entity", auditGrammar);
		PrintWriter writer = new PrintWriter(System.out);
		
		Grammar metaGrammar = Parser.parseGrammar(amstin.models.meta._Main.METAMODEL_MDG);
		Unparse.unparse(metaGrammar, auditMetaModel, writer);
		writer.flush();
		
		

		File file = new File(Config.ROOT);
		
		MetaModelToJava.metaModelToJava(file, AUDIT_PKG, auditMetaModel);
		
		
		String example = Parser.readPath(EXAMPLE_AUDIT);
		Parser auditParser = new Parser(auditGrammar);
		
		Object exampleModel = auditParser.parse(AUDIT_PKG, example);
		System.out.println(exampleModel);
		
		Unparse.unparse(auditGrammar, exampleModel, writer);
		writer.flush();
		
		
		FileWriter dot = new FileWriter(new File("basic.dot"));
		ToDot.toDot(exampleModel, dot);
		dot.close();
		
	}
	
	
}

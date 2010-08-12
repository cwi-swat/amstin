package amstin.models.module;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import amstin.Config;
import amstin.models.ast.Tree;
import amstin.models.grammar.Grammar;
import amstin.models.grammar.parsing.mj.Parser;
import amstin.models.meta.MetaModel;
import amstin.tools.ASTtoModel;
import amstin.tools.GrammarToMetaModel;
import amstin.tools.MetaModelToJava;
import amstin.tools.ModelToString;

public class _Main {

	private static final String MODULE_PKG = Config.PKG +".models.module";
	private static final String MODULE_MDG = Config.PKG_DIR + "/models/module/boot_module.mdg";
	
	private static final String META_MODULE = Config.PKG_DIR + "/models/module/meta.module";
	
//	private static final String METAMODEL_MDG = Config.PKG_DIR + "/models/meta/metamodel.mdg";

	
	public static void main(String[] args) throws IOException {
		Grammar moduleGrammar = Parser.parseGrammar(MODULE_MDG);
		System.out.println(moduleGrammar);
		
		
		MetaModel moduleMetaModel = GrammarToMetaModel.infer("Module", moduleGrammar);
		PrintWriter writer = new PrintWriter(System.out);
		
		Grammar metaGrammar = Parser.parseGrammar(amstin.models.meta._Main.METAMODEL_MDG);
		ModelToString.unparse(metaGrammar, moduleMetaModel, writer);
		writer.flush();
		
		File file = new File(Config.ROOT);
		
		MetaModelToJava.metaModelToJava(file, MODULE_PKG, moduleMetaModel);
		
		
		String meta = Parser.readPath(META_MODULE);
		Parser moduleParser = new Parser(moduleGrammar);
		Tree obj = moduleParser.parse(meta);
		
		// Import map:
		// instance -> java.pkg.namespace
		// (only works for direct imports)
		// instantiate uses this map to find defined names (using key) and knows
		// where to find java classes
		// so the input to Instantiate is a map from instance to pkg
		
		// Grammar and Rule are then defined in amstin.models.grammar
		//
		
		System.out.println(obj);
		Module metaModule = (Module) ASTtoModel.instantiate(MODULE_PKG, obj);
		System.out.println(metaModule);
//		
//		PrintWriter out = new PrintWriter(System.out);
//		CreateScript.script("a2mtk.models.module", "Grant", metaModule, out);
//		out.flush();
//		
//		
//		Grammar meta = Parser.parseGrammar(amstin.models.meta._Main.METAMODEL_MDG);
//		String moduleMeta = Parser.readPath(MODULE_META);
//		Parser metaParser = new Parser(meta);
//		MetaModel moduleMetaModel = (MetaModel)metaParser.parse(amstin.models.meta._Main.METAMODEL_PKG, moduleMeta);
//		
//		List<String> errors = CheckInstance.checkInstance(moduleMetaModel, metaModule);
//		for (String error: errors) {
//			System.out.println("ERROR: " + error);
//		}
//		
//		
//		PrintWriter writer = new PrintWriter(System.out);
//		Unparse.unparse(module, metaModule, writer);
//		writer.flush();
//		
//		FileWriter f = new FileWriter(new File("module.dot"));
//		ToDot.toDot(metaModule, f);
//		f.flush();
		
	}
}

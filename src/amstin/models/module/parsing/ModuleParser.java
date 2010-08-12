package amstin.models.module.parsing;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import amstin.Config;
import amstin.models.grammar.parsing.mj.Parser;

public class ModuleParser {
	
	private static final String MODULE_MDG = Config.PKG_DIR + "/models/module/boot_module.mdg";
	private static final String META_MODULE = Config.PKG_DIR + "/models/module/Module.mod";
	private static final String MODULE_EXTENSION = ".mod";

	
	public static void main(String[] args) {
		String src = Parser.readPath(META_MODULE);
		ModuleParser m = new ModuleParser(new File("."), src);
		Object ast = m.parse("module Bla: Grammar import grammar.Grammar.*; import bla.foo.BarModel; ");
		System.out.println("AST = " + ast);
	}

	private String name;
	private String syntax;
	private Set<String> importedModules;
	private Set<String> importedGlobs;
	private String src;
	private File root;
	
	public ModuleParser(File root, String src) {
		this.root = root;
		this.src = src;
		this.importedModules = new HashSet<String>();
		this.importedGlobs = new HashSet<String>();
	}
	
	
	private File importNameToPath(String moduleName) {
		String dirs[] = moduleName.split("\\.");
		if (dirs.length <= 1) {
			throw new AssertionError("Invalid moduleName " + moduleName);
		}
		String path = dirs[0];
		for (int i = 1; i < dirs.length; i++) {
			path += File.separator + dirs[1];
		}
		return new File(root, path);
	}


	private File importNameToFile(String moduleName) {
		File path = importNameToPath(moduleName);
		File dir = path.getParentFile();
		String moduleFile = path.getName() + MODULE_EXTENSION;
		return new File(dir, moduleFile);
	}
	
	private String readImportModule(String moduleName) {
		File module = importNameToFile(moduleName);
		return Parser.readPath(module.getAbsolutePath());
	}
	
	
	
	private Object parse(String string) {
		parseHeader(string);
		return null;
	}


	
	private String parseHeader(String src) {
		String mod = "(" + Parser.ID_REGEX + ")" + "(\\."  + Parser.ID_REGEX + ")+";
		String regex = "^\\s*module\\s+(" + mod + ")\\s*:\\s*(" + mod + ")";
		Pattern pattern = Pattern.compile(regex);
		Matcher m = pattern.matcher(src);
		if (!m.lookingAt()) {
			throw new RuntimeException("Cannot parse header: " + src);
		}
		this.name = m.group(1);
		this.syntax = m.group(4);
		
//		System.out.println("Name = " + name + "; syntax = " + syntax);
		
		String importOne = "import\\s+(" + mod + ")";
		String importAll = "import\\s+(" + mod + "\\s*\\.\\s*\\*)";
		String importBoth = "^\\s*(" + importOne + "|"  + importAll + ")\\s*;";
		
		pattern = Pattern.compile(importBoth);
		
		
		int endPos = m.end();
		src = src.substring(endPos);
		m = pattern.matcher(src);
		
		while (m.lookingAt()) {
//			for (int i = 1; i <= m.groupCount(); i++) {
//				System.out.println("GROUP " + i + ": " + m.group(i));
//			}
			String one = m.group(2);
			String all = m.group(5);
			if (one != null) {
				importedModules.add(one);
			}
			else if (all != null) {
				importedGlobs.add(all);
			}
			else {
				throw new AssertionError("Inconsistent regular expression " + importBoth);
			}
			endPos = m.end();
			src = src.substring(endPos);
			m = pattern.matcher(src);
		}
		
		return src;
		
	}
	

}

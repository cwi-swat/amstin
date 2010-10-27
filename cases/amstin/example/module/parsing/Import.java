package amstin.example.module.parsing;

import java.io.File;

public class Import {

	private String name;
	private File file;

	public Import(String importName) {
		String comps[] = importName.split("\\.");
		name = comps[comps.length - 1];
		String path = comps[0];
		for (int i = 1; i < comps.length - 2; i++) {
			path += File.separator + comps[i];
		}
		this.file = new File(path);
	}
	
	public File getFile() {
		return file;
	}
	
	public String getName() {
		return name;
	}
	
}

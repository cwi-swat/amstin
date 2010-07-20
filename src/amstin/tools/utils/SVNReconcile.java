package amstin.tools.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Set;

public class SVNReconcile {
	
	private Set<File> existing;
	private Set<File> generated;

	public SVNReconcile(Set<File> existing, Set<File> generated) {
		this.existing = existing;
		this.generated = generated;
	}
	
	public void prepare() {
		// do backups if files are modified.
	}
	
	
	private static void svn(String arg) {
		try {
			String line;
			Process p = Runtime.getRuntime().exec("svn " + arg);
			BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
			while ((line = input.readLine()) != null) {
				System.out.println(line);
			}
			input.close();
		}
		catch (Exception err) {
			err.printStackTrace();
		}
	}

	public void reconcile() {
		for (File f: generated) {
			String wcpath = f.toString();
			if (existing.contains(f)) {
				System.out.println("Merging " + wcpath + "...");
				svn("merge " + wcpath); 
			}
			else {
				System.out.println("Adding " + wcpath + " to SVN");
				svn("add " + wcpath);
			}
		}
	}
	
}

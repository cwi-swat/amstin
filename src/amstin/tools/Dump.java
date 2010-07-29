package amstin.tools;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.yaml.snakeyaml.Yaml;

public class Dump {
	
	public static void dump(Object obj, File file) {
		FileWriter writer;
		try {
			writer = new FileWriter(file);
			dump(obj, writer);
			writer.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static void dump(Object obj, Writer writer) {
		Yaml y = new Yaml();
		y.dump(obj, writer);
	}
}

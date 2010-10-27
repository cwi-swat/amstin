package amstin.example.template.render;

import java.io.IOException;
import java.io.Writer;

import amstin.example.template.Statement;

public class Closure {

	private Env env;
	private Statement block;

	public Closure(Env env, Statement block) {
		this.env = env;
		this.block = block;
	}
	
	public void eval(Writer output) throws IOException {
		block.eval(env, output);
	}
	
}

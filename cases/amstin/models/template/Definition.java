
package amstin.models.template;

import java.io.IOException;
import java.io.Writer;

import amstin.models.template.render.Env;


public class Definition {

    public String name;
    public Formals formals;
    public Statement body;
    
	public void eval(Env env, Args args, Writer output) throws IOException {
		body.eval(bindArgs(env, args), output);
	}

	private Env bindArgs(Env env, Args args) {
		if (args == null) {
			return env;
		}
		
		Object objs[] = new Object[args.args.size()];
		int i = 0;
		for (Arg arg: args.args) {
			if (arg instanceof Actual) {
				objs[i++] = ((Actual)arg).expression.eval(env);
			}
			else {
				throw new RuntimeException("keyword args not yet implemented");
			}
		}

		env = new Env(env);
		for (int j = 0; j < objs.length; j++) {
			if (j < formals.params.size()) {
				env.store(formals.params.get(j).name, objs[j]);
			}
		}
		return env;
	}

}

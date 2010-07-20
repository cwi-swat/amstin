
package amstin.models.template;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import amstin.models.template.utils.Env;


public class Definition {

    public String name;
    public Formals formals;
    public Statement body;
    
	public void eval(Env env, List<Args> args, Writer output) throws IOException {
		if (args != null) {
			Object objs[] = new Object[args.size()];
			int i = 0;
			for (Arg arg: args.get(0).args) {
				if (arg instanceof Actual) {
					objs[i++] = ((Actual)arg).expression.eval(env);
				}
				else {
					throw new RuntimeException("not yet implemented");
				}
			}

			env = new Env(env);
			for (int j = 0; j < objs.length; j++) {
				if (j < formals.params.size()) {
					env.store(formals.params.get(j).name, objs[j]);
				}
			}
		}
		body.eval(env, output);
	}

}

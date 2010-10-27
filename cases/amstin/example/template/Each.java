
package amstin.example.template;

import java.io.IOException;
import java.io.Writer;

import amstin.example.template.render.Env;


@SuppressWarnings("unchecked")
public class Each
    extends Statement
{

    public String var;
    public Expression iter;
    public Statement body;
	@Override
	public void eval(Env env, Writer output) throws IOException {
		Iterable i = (Iterable) iter.eval(env);
		env = new Env(env);
		for (Object x: i) {
			env.store(var, x);
			body.eval(env, output);
		}
	}

}

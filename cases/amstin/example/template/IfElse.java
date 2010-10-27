
package amstin.example.template;

import java.io.IOException;
import java.io.Writer;

import amstin.example.template.render.Env;


public class IfElse
    extends Statement
{

    public Expression cond;
    public Statement then;
    public Statement otherwise;
    
	@Override
	public void eval(Env env, Writer output) throws IOException {
		Object val = cond.eval(env);
		if (val.equals(true)) {
			then.eval(env, output);
		}
		else {
			otherwise.eval(env, output);
		}
	}

}

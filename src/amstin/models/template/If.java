
package amstin.models.template;

import java.io.IOException;
import java.io.Writer;

import amstin.models.template.render.Env;


public class If
    extends Statement
{

    public Expression cond;
    public Statement then;
    
	@Override
	public void eval(Env env, Writer output) throws IOException {
		Object val = cond.eval(env);
		if (val.equals(true)) {
			then.eval(env, output);
		}
	}

}

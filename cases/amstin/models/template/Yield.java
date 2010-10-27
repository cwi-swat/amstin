
package amstin.models.template;

import java.io.IOException;
import java.io.Writer;

import amstin.models.template.render.Closure;
import amstin.models.template.render.Env;


public class Yield
    extends Statement
{
	@Override
	public void eval(Env env, Writer output) throws IOException {
		Closure clos = (Closure)env.lookup("yield");
		clos.eval(output);
	}


}

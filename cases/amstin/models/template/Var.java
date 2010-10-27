
package amstin.models.template;

import amstin.models.template.render.Env;


public class Var
    extends Expression
{

    public String name;

	@Override
	public Object eval(Env env) {
		return env.lookup(name);
	}

}

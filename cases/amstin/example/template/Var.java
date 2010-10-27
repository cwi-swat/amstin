
package amstin.example.template;

import amstin.example.template.render.Env;


public class Var
    extends Expression
{

    public String name;

	@Override
	public Object eval(Env env) {
		return env.lookup(name);
	}

}

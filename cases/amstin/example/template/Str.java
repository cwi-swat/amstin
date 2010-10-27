
package amstin.example.template;

import amstin.example.template.render.Env;


public class Str
    extends Expression
{

    public String value;

	@Override
	public Object eval(Env env) {
		return value;
	}

}

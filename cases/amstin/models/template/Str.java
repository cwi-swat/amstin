
package amstin.models.template;

import amstin.models.template.render.Env;


public class Str
    extends Expression
{

    public String value;

	@Override
	public Object eval(Env env) {
		return value;
	}

}

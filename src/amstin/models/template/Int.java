
package amstin.models.template;

import amstin.models.template.utils.Env;


public class Int
    extends Expression
{

    public Integer value;

	@Override
	public Object eval(Env env) {
		return value;
	}

    
    
}

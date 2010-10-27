
package amstin.example.template;

import amstin.example.template.render.Env;


public class Int
    extends Expression
{

    public Integer value;

	@Override
	public Object eval(Env env) {
		return value;
	}

    
    
}

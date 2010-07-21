
package amstin.models.template;

import amstin.models.template.render.Env;


public abstract class Expression {

	public abstract Object eval(Env env);


}

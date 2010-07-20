
package amstin.models.template;

import java.io.IOException;
import java.io.Writer;

import amstin.models.template.utils.Env;


public abstract class Statement {

	public abstract void eval(Env env, Writer output) throws IOException;


}

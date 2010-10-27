
package amstin.example.template;

import java.io.IOException;
import java.io.Writer;

import amstin.example.template.render.Env;


public abstract class Statement {

	public abstract void eval(Env env, Writer output) throws IOException;


}

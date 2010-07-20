
package amstin.models.template;

import java.io.Writer;
import java.util.List;

import amstin.models.template.utils.Env;


public class Definition {

    public String name;
    public Formals formals;
    public Statement body;
    
	public void eval(Env env, List<Args> args, Writer output) {
		// TODO
	}

}

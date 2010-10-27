
package amstin.models.template;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import amstin.models.template.render.Env;

public class Definitions {

    public List<Definition> definitions;

    public void eval(Writer output, Object ...objs) throws IOException {
    	Env env = new Env();
    	Definition main = null;
    	for (Definition def: definitions) {
    		env.store(def.name, def);
    		if (def.name.equals("main")) {
    			main = def;
    		}
    	}
    	if (main != null) {
    		int i = 0;
    		for (Param x : main.formals.params) {
    			env.store(x.name, objs[i++]);
    		}
    		main.body.eval(env, output);
    	}
    }
    
}

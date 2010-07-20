
package amstin.models.template;

import java.io.IOException;
import java.io.Writer;

import amstin.models.template.utils.Closure;
import amstin.models.template.utils.Env;

public class Element
    extends Statement
{

    public Tag tag;
    public Args args;
    public Statement body;
    
    @Override
    public void eval(Env env, Writer output) throws IOException {
    	Object val = env.lookup(tag.name);
		if (val != null) {
    		Definition def = (Definition)val;
    		env = new Env(env);
    		env.store("yield", new Closure(env, body));
    		def.eval(env, args, output);
    	}
    	else {
    		// TODO: attrs and suffixes
    		output.write("<" + tag.name + ">");
    		body.eval(env, output);
    		output.write("</" + tag.name + ">");
    	}
    }

}

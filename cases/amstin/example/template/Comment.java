
package amstin.example.template;

import java.io.IOException;
import java.io.Writer;

import amstin.example.template.render.Env;


public class Comment
    extends Statement
{

    public Expression expression;

	@Override
	public void eval(Env env, Writer output) throws IOException {
		output.write("<!--");
		output.write(expression.eval(env).toString());
		output.write("-->");		
	}


}

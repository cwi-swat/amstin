
package amstin.models.template;

import java.io.IOException;
import java.io.Writer;

import amstin.models.template.render.Env;


public class CData
    extends Statement
{

    public Expression expression;

	@Override
	public void eval(Env env, Writer output) throws IOException {
		output.write("<![CDATA[");
		output.write(expression.eval(env).toString());
		output.write("]]>");
	}

}

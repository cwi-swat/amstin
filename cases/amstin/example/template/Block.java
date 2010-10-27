
package amstin.example.template;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import amstin.example.template.render.Env;

public class Block
    extends Statement
{

    public List<Statement> statements;

	@Override
	public void eval(Env env, Writer output) throws IOException {
		for (Statement stat: statements) {
			stat.eval(env, output);
		}
	}

}

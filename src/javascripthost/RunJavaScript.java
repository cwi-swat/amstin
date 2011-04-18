package javascripthost;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;


public class RunJavaScript {

	public static void main(String[] args) {
		Context cx = Context.enter();
		Scriptable scope = cx.initStandardObjects();
		Object wrappedOut = Context.javaToJS(System.out, scope);
		ScriptableObject.putProperty(scope, "out", wrappedOut);
		ScriptableObject.putProperty(scope, "x", new Test());
		Object result = cx.evaluateString(scope, "out.print(x.foo + ' ' + x.tijs)", "<cmd>", 1, null);
	}
}

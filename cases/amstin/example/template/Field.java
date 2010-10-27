
package amstin.example.template;

import amstin.example.template.render.Env;


public class Field
    extends Expression
{

    public Expression expression;
    public String field;
    
	@Override
	public Object eval(Env env) {
		Object val = expression.eval(env);
		try {
			java.lang.reflect.Field f = val.getClass().getField(field);
			return f.get(val);
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (NoSuchFieldException e) {
			throw new RuntimeException(e);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

}

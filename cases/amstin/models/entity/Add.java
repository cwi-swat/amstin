
package amstin.models.entity;

import amstin.models.entity.eval.Obj;


public class Add
    extends Expression
{

    public Expression lhs;
    public Expression rhs;

    @Override
	public Object eval(Obj self) {
		Object x = lhs.eval(self);
		Object y = rhs.eval(self);
		if (x instanceof Integer && y instanceof Integer) {
			return ((Integer)x) + ((Integer)y);
		}
		throw new RuntimeException("Cannot add non-integer operands");
	}
    
}

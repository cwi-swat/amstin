
package amstin.models.entity;

import amstin.models.entity.eval.Obj;


public class Const
    extends Expression
{

    public Value value;

	@Override
	public Object eval(Obj self) {
		return value.eval();
	}
    
    

}

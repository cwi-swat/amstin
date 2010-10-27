
package amstin.example.entity;

import amstin.example.entity.eval.Obj;


public class Const
    extends Expression
{

    public Value value;

	@Override
	public Object eval(Obj self) {
		return value.eval();
	}
    
    

}

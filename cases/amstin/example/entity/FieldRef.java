
package amstin.example.entity;

import amstin.example.entity.eval.Obj;


public class FieldRef
    extends Expression
{

    public String name;

	@Override
	public Object eval(Obj self) {
		return self.get(name);
	}

}

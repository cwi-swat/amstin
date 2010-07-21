
package amstin.models.entity;

import amstin.models.entity.eval.Obj;


public class FieldRef
    extends Expression
{

    public String name;

	@Override
	public Object eval(Obj self) {
		return self.get(name);
	}

}

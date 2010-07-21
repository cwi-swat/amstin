
package amstin.models.entity;


public class BoolValue
    extends Value
{

    public Bool value;

	@Override
	public Object eval() {
		return value.eval();
	}

}

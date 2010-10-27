
package amstin.models.entity;


public class BoolValue
    extends Value
{

    public Boolean value;

	@Override
	public Object eval() {
		return value;
	}

}

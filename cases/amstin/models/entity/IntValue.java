
package amstin.models.entity;


public class IntValue
    extends Value
{

    public Integer value;

	@Override
	public Object eval() {
		return value;
	}

}

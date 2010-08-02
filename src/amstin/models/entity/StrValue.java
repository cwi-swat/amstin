
package amstin.models.entity;


public class StrValue
    extends Value
{

    public String value;

	@Override
	public Object eval() {
		return value;
	}

}

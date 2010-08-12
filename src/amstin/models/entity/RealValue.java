package amstin.models.entity;

public class RealValue extends Value {

	public Double value;
	
	@Override
	public Object eval() {
		return value;
	}

}

package amstin.parsing.ast;

public class Field implements Visitable {

	private String name;
	private Object value;

	public Field(String name, Object value) {
		this.name = name;
		this.value = value;
	}
	
	public String getName() {
		return name;
	}

	public Object getValue() {
		return value;
	}
	
	@Override
	public String toString() {
		return getName() + ": " + getValue();
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}
}

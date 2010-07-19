package amstin.models.meta;

import java.util.List;

public class Class extends Type {

	public String name;
	public Boolean isAbstract = false;
	public Parent parent = null;
	public List<Field> fields;
	
	@Override
	public String toString() {
		return "class(" + name + ", " + isAbstract + ", " + parent + ")";
	}
}

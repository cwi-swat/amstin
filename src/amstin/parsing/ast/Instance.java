package amstin.parsing.ast;

import java.util.List;

public class Instance implements Visitable {

	private String name;
	private List<Object> args;

	public Instance(String name, List<Object> args) {
		this.name = name;
		this.args = args;
	}
	
	@Override
	public String toString() {
		return "instance(" + name + ", " + args + ")";
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}
	
	public List<Object> getArgs() {
		return args;
	}

	public String getType() {
		return name;
	}

}

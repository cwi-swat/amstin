package amstin.models.grammar.parsing.ast;

public class Str {
	
	// TODO: remove this class
	// it does not match with meta models
	
	private String str;

	public Str(String str) {
		this.str = str;
	}

	@Override
	public String toString() {
		return "\"" + str + "\"";
	}

	public Object getValue() {
		return str;
	}
	
}

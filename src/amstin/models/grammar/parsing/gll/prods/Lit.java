package amstin.models.grammar.parsing.gll.prods;

public class Lit extends Production {

	private String value;

	public Lit(String value) {
		this.value = value;
	}
	
	@Override
	public String toString() {
		return value;
	}
	
}

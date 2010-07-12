package amstin.parsing.ast;

public class Reference implements Visitable {

	private Symbol symbol;
	private String type;

	public Reference(Symbol symbol, String type) {
		this.symbol = symbol;
		this.type = type;
	}
	
	@Override
	public String toString() {
		return "*" + symbol + ":" + type;
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}
	
	public Symbol getSymbol() {
		return symbol;
	}
	
	public String getType() {
		return type;
	}

}

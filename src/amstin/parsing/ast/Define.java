package amstin.parsing.ast;

public class Define implements Visitable {
	
	private Symbol symbol;

	public Define(Symbol symbol) {
		this.symbol = symbol;
	}
	
	public Symbol getSymbol() {
		return symbol;
	}
	
	@Override
	public String toString() {
		return "&" + symbol;
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}
	

}

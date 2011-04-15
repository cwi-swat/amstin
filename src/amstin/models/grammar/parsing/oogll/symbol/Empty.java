package amstin.models.grammar.parsing.oogll.symbol;


public class Empty extends Base implements Symbol {
	public static final Empty EMPTY = new Empty();
	
	private Empty() {
	}
	
	@Override
	public String toString() {
		return "<>"; //"Û";
	}


	@Override
	public boolean isNullable() {
		return true;
	}
	
//	@Override
//	public boolean isInFirst(char c) {
//		return false;
//	}


}

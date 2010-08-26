package amstin.models.grammar.parsing.oogll.sppf;


public interface INode extends Iterable<INode> {
	public String getShape();
	public String getLabel();

}

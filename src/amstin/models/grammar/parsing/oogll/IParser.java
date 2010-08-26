package amstin.models.grammar.parsing.oogll;

import amstin.models.grammar.parsing.oogll.sppf.Node;

public interface IParser {
	void parse(int pos, GSS cu, Node cn, GLL ctx);
}

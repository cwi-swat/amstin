package amstin.models.grammar.parsing.gll;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URI;

import amstin.models.grammar.parsing.gll.stack.AbstractStackNode;
import amstin.models.parsetree.Tree;

public interface IGLL{
	public final static int START_SYMBOL_ID = -1;
	
	public final static int DEFAULT_LIST_EPSILON_ID = -2; // (0xeffffffe | 0x80000000)
	
	Tree parse(AbstractStackNode start, URI inputURI, char[] input);
	Tree parse(AbstractStackNode start, URI inputURI, String input);
	Tree parse(AbstractStackNode start, URI inputURI, InputStream in) throws IOException;
	Tree parse(AbstractStackNode start, URI inputURI, Reader in) throws IOException;
	Tree parse(AbstractStackNode start, URI inputURI, File inputFile) throws IOException;
}

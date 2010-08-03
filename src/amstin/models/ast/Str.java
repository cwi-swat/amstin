
package amstin.models.ast;


public class Str
    extends Tree
{

    public String value;
    public Location loc;

    @Override
    public String toString() {
    	// TODO: escaping;
    	return "\"" + value + "\"";
    }
}

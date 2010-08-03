
package amstin.models.ast;


public class False
    extends Tree
{

    public Location loc;

    @Override
    public String toString() {
    	return "false";
    }
}

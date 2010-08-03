
package amstin.models.ast;


public class True
    extends Tree
{

    public Location loc;

    @Override
    public String toString() {
    	return "true";
    }
}


package amstin.models.ast;


public class Ref
    extends Tree
{

    public String name;
    public String type;
    public Location loc;

    @Override
    public String toString() {
    	return "*" + name + "#" + type;
    }
}

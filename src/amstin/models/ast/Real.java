
package amstin.models.ast;


public class Real
    extends Tree
{

    public Double value;
    public Location loc;

    @Override
    public String toString() {
    	return value.toString();
    }
}

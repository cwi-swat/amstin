
package amstin.models.ast;


public class Arg
    extends Tree
{

    public String name;
    public Tree value;

    @Override
    public String toString() {
    	return name + ": " + value;
    }
    
}

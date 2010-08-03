
package amstin.models.ast;


public class List
    extends Tree
{

    public java.util.List<Tree> elements;

    @Override
    public String toString() {
    	return elements.toString();
    }
}

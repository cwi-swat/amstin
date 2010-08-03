
package amstin.models.ast;

import java.util.List;

public class Obj
    extends Tree
{

    public String name;
    public List<Tree> args;

    @Override
    public String toString() {
    	String s = name + "(";
    	for (int i = 0; i < args.size(); i++) {
    		Tree t = args.get(i);
    		s += t.toString();
    		if (i < args.size() - 1) {
    			s += ", ";
    		}
    	}
    	return s + ")";
    }
}

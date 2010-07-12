package amstin.tools;

import java.io.Writer;

public class ToDot {

	public static void script(String pkg, String name, Object obj, Writer output) {
		ToDot inst = new ToDot(obj, output);
		inst.todot();
	}


	public ToDot(Object obj, Writer output) {
		// TODO Auto-generated constructor stub
	}

	private void todot() {
		// TODO Auto-generated method stub
		
	}




}

package amstin.tools;

import java.io.Writer;
import java.util.IdentityHashMap;

public class ToDot {

	public static void script(String pkg, String name, Object obj, Writer output) {
		ToDot inst = new ToDot(obj, output);
		inst.todot();
	}

	private Object obj;
	private Writer output;
	private IdentityHashMap<Object, Integer> visited;
	private int node;


	private ToDot(Object obj, Writer output) {
		this.obj = obj;
		this.output = output;
		this.visited = new IdentityHashMap<Object,Integer>();
		this.node = 0;
	}

	private void todot() {
		// TODO Auto-generated method stub
		
		// shape=MRecord, label=< <table>
		
		/*
		 * <table>
		 * <tr> identity: type </tr>
		 * <tr border="top"> 
		 */
		
	}




}

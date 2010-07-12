package amstin.parsing;

import java.util.ArrayList;
import java.util.List;

public class Entry {
	final List<Cnt> cnts;
	final List<Integer> results;
	final List<Object> objects;
	
	Entry() {
		this.cnts = new ArrayList<Cnt>();
		this.results = new ArrayList<Integer>();
		this.objects = new ArrayList<Object>();
	}
	
	public boolean isSubsumed(int result, Object object) {
		for (int i = 0; i < results.size(); i++) {
			if (results.get(i) == result && objects.get(i) == object) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String toString() {
		return "entry(" + cnts + ", " + results + ")";
	}
	

	
}

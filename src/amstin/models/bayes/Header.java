
package amstin.models.bayes;

import java.util.List;

public class Header {

    public String name;
    public List<String> colNames;

    public int size() {
		return colNames.size();
	}

	public int getPosition(String columnName) {
		return colNames.indexOf(columnName);
	}

}

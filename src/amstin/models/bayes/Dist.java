
package amstin.models.bayes;

import java.util.List;

import org.ejml.data.SimpleMatrix;

public class Dist
    extends Decl
{

    public Header header;
    public List<Row> rows;
    
    
    public void check() {
    	int n = header.size();
    	int i = 0;
    	for (Row row: rows) {
    		if (row.rowValues.size() != n) {
    			throw new RuntimeException("Row " + i + " of " + header.name + " does not contain " + n + " elements, as specified in the header");
    		}
    		i++;
    	}
    }
    
    public double[] lookUp(String columnName) {
    	// TODO: cache positions for names in a table and use the matrix
    	int j = header.getPosition(columnName);
    	double result[] = new double[rows.size()];
    	int i = 0;
    	for (Row row: rows) {
    		result[i] = row.get(j);
    		i++;
    	}
    	return result;
    }
    
    public SimpleMatrix getMatrix() {
    	SimpleMatrix m = new SimpleMatrix(rows.size(), header.size());
    	for (int i = 0; i < rows.size(); i++) {
    		for (int j = 0; i < header.size(); j++) {
    			m.set(i, j, rows.get(i).get(j));
    		}
    	}
    	return m;
    }

}

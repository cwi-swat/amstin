
package amstin.models.entity;

import java.util.List;

import amstin.models.entity.eval.Obj;

public class Entity {

    public String name;
    public List<Field> fields;

    
    public Obj create() {
    	return new Obj(this);
    }
    
}

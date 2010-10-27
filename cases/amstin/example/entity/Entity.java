
package amstin.example.entity;

import java.util.List;

import amstin.example.entity.eval.Obj;

public class Entity {

    public String name;
    public List<Field> fields;

    
    public Obj create() {
    	return new Obj(this);
    }
    
}

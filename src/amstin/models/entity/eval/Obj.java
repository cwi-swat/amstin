package amstin.models.entity.eval;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import amstin.models.entity.Derived;
import amstin.models.entity.Entity;
import amstin.models.entity.Field;

public class Obj {

	private Entity type;
	private Map<String, Object> table;

	// TODO: inverses
	
	public Obj(Entity type) {
		this.type = type; 
		this.table = new HashMap<String, Object>();
		for (Field field: type.fields) {
			table.put(field.name, field.defaultValue());
		}
	}
	
	public Entity getType() {
		return type;
	}

	
	public void set(String name, Object value) {
		Field field = findField(name);

		if (field.isDerived()) {
			throw new RuntimeException("Cannot assign to derived field");
		}
		
		if (value == null && !field.isOptional()) {
			throw new RuntimeException("Cannot assign null to non-optional field");
		}
		
		if (field.isMany()) {
			throw new RuntimeException("Cannot assign to 'many' fields.");
		}

		field.checkType(value);
		
		table.put(field.name, value);
	}
	
	public Object get(String name) {
		Field field = findField(name);
		
		if (field.isDerived()) {
			Derived d = field.getDerived();
			return d.expression.eval(this);
		}
		
		return table.get(field.name);
	}
	
	
	@SuppressWarnings("unchecked")
	public void add(String name, Object value) {
		Field field = findField(name);
		checkIfMany(field, value);
		((Collection)table.get(field.name)).add(value);
	}
	
	@SuppressWarnings("unchecked")
	public void remove(String name, Object value) {
		Field field = findField(name);
		checkIfMany(field, value);
		((Collection)table.get(field.name)).remove(value);
	}

	private void checkIfMany(Field field, Object value) {
		if (!field.isMany()) {
			throw new RuntimeException("Cannot add/remove to non-'many' fields.");
		}
		field.checkType(value);
	}

	
	private Field findField(String name) {
		for (Field field: type.fields) {
			if (name.equals(field.name)) {
				return field;
			}
		}
		throw new RuntimeException("No such field: " + name);
	}
	

}

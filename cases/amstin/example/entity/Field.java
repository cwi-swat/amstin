
package amstin.example.entity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import amstin.example.entity.eval.Obj;

public class Field {

    public String name;
    public Type type;
    public List<Modifier> modifiers;

    
	public boolean isBool() {
		return type instanceof BoolType;
	}

	public boolean isInt() {
		return type instanceof IntType;		
	}

	public boolean isStr() {
		return type instanceof StrType;
	}

	public boolean isAssoc() {
		return type instanceof AssocType;
	}

	public Entity getAssocTarget() {
		return ((AssocType)type).entity;
	}
	
	public boolean isMany() {
		return hasModifier(Many.class);
	}


	public boolean isOrdered() {
		return hasModifier(Ordered.class);
	}
	
	public boolean isOptional() {
		return hasModifier(Optional.class);
	}
	
	public boolean isDerived() {
		return getDerived() != null;
	}
	
	public Derived getDerived() {
		return (Derived)getModifier(Derived.class);
	}

	private boolean hasDefault() {
		return getDefault() != null;
	}

	private Default getDefault() {
		return (Default) getModifier(Default.class);
	}


	public boolean isUnique() {
		return hasModifier(Unique.class);
	}

	private boolean hasModifier(Class<? extends Modifier> klass) {
		return getModifier(klass) != null;
	}
	
	private Modifier getModifier(Class<? extends Modifier> klass) {
		for (Modifier m: modifiers) {
			if (klass.equals(m.getClass())) {
				return m;
			}
		}
		return null;
	}


	public Object defaultValue() {
		if (isMany() && isOrdered()) {
			return new ArrayList<Object>();
		}
		if (isMany() && !isOrdered() && isUnique()) {
			return new HashSet<Object>();
		}
		if (isMany() && !isOrdered() && !isUnique()) {
			return new ArrayList<Object>();
		}
		
		if (hasDefault()) {
			Default d = getDefault();
			Object value = d.value.eval();
			checkType(value);
			return value;
		}
		
		return type.defaultValue();
	}

	
	public boolean isCompatible(Object value) {
		return (isBool() && value instanceof Boolean) 
			|| (isStr() && value instanceof String)
			|| (isInt() && value instanceof Integer)
			|| (isAssoc() && value instanceof Obj && ((Obj)value).getType() == getAssocTarget());
	}
	
	public void checkType(Object value) {
		if (!isCompatible(value)) {
			throw new RuntimeException("Illegal default value " + value + " for field " + this);
		}
	}


}

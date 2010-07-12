package amstin.tools;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;

import amstin.models.meta.Bool;
import amstin.models.meta.Class;
import amstin.models.meta.Field;
import amstin.models.meta.Int;
import amstin.models.meta.Klass;
import amstin.models.meta.MetaModel;
import amstin.models.meta.Opt;
import amstin.models.meta.Star;
import amstin.models.meta.Str;
import amstin.models.meta.Type;


public class CheckInstance {

	private IdentityHashMap<Object, Class> done;
	private Class klass;
	private Object root;
	private List<String> errors;
	private MetaModel metaModel;


	public static List<String> checkInstance(MetaModel metaModel, Object obj) {
		CheckInstance check = new CheckInstance(metaModel, obj);
		return check.errors();
	}

	private static Class findClass(MetaModel metaModel, String simpleName) {
		Class klass = null;
		for (Class c: metaModel.classes) {
			if (simpleName.equals(c.name)) {
				klass = c;
				break;
			}
		}
		return klass;
	}

	
	private CheckInstance(MetaModel metaModel, Object obj) {
		this.metaModel = metaModel;
		this.done = new IdentityHashMap<Object, Class>();
		this.klass = findClass(metaModel, obj.getClass().getSimpleName());
		this.root = obj;
		this.errors = new ArrayList<String>();
	}

	private List<String> errors() {
		check(klass, root);
		return errors;
	}
	
	private Class findSubClassOf(String superType, String name) {
		// TODO: we only support direct subclasses, no transitivity
		for (Class c: metaModel.classes) {
			if (name.equals(c.name) && name.equals(superType)) {
				// searching for superType itself;
				return c;
			}
			if (name.equals(c.name) && c.parent != null && superType.equals(c.parent.type.name)) {
				return c;
			}
		}
		return null;
	}
	
	private void check(Type type, Object obj) {
		if (done.containsKey(obj)) {
			return;
		}

		if (type instanceof Klass) {
			type = ((Klass)type).klass;
		}
		
		if (type instanceof Class) {
			Class cls = (Class)type;
			done.put(obj, cls);
			checkClass(cls, obj);
		}
		else if (type instanceof Str && !(obj instanceof String)) {
			errors.add("Object " + obj + " should have type String");
		}
		else if (type instanceof Int && !(obj instanceof Integer)) {
			errors.add("Object " + obj + " should have type Integer");
		}
		else if (type instanceof Bool && !(obj instanceof Boolean)) {
			errors.add("Object " + obj + " should have type Boolean");
		}
	}

	private void checkClass(Class cls, Object obj) {
		boolean found = false;
		java.lang.Class<? extends Object> klz = obj.getClass();
		do {
			if (cls.name.equals(klz.getSimpleName())) {
				found = true;
				break;
			}
			klz = klz.getSuperclass();
		} while (klz != null);
		if (!found) {
			errors.add("Object " + obj + " should have class " + cls.name);
			return;
		}
		// Don't use klz here, we want the actual class of obj, not it's inherited root.
		String actualCls = obj.getClass().getSimpleName();
		Class concreteCls = findSubClassOf(cls.name, actualCls);
		if (concreteCls == null) {
			errors.add("No concrete subclass of " + cls.name + " corresponding to " + actualCls);
		}
		else {
			for (Field f: concreteCls.fields) {
				errorsForField(f, obj);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void errorsForField(Field f, Object obj) {
		java.lang.Class<? extends Object> klazz = obj.getClass();
		for (java.lang.reflect.Field field: klazz.getFields()) {
			Object value;
			try {
				value = field.get(obj);
			} catch (IllegalArgumentException e) {
				throw new RuntimeException(e);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}
			String name = field.getName();
			if (name.equals(f.name)) {
				if (value == null && !(f.mult instanceof Opt)) {
					errors.add("Field " + field + " is null but does not have multiplicity ?");
				}
				else if (value == null && f.mult instanceof Opt) {
					return;
				}
				else if (value instanceof List && !(f.mult instanceof Star)) {
					errors.add("Field " + field + " contains a List but does not have multiplicity *");
				}
				else if (!(value instanceof List) && f.mult instanceof Star) {
					errors.add("Field " + field + " is declared with multiplicity * but does not contain a List");
				}
				else if (value instanceof List && f.mult instanceof Star) {
					List list = (List)value;
					for (Object x: list) {
						check(f.type, x);
					}
				}
				else {
					check(f.type, value);
				}
				return; // found the field, done the checks; return.
			}
		}
		errors.add("Object " + obj + " does not have field " + f.name);
	}
}

package amstin.tools;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.List;

import amstin.models.meta.Bool;
import amstin.models.meta.Class;
import amstin.models.meta.Field;
import amstin.models.meta.Int;
import amstin.models.meta.Klass;
import amstin.models.meta.MetaModel;
import amstin.models.meta.Mult;
import amstin.models.meta.Single;
import amstin.models.meta.Str;
import amstin.models.meta.Type;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JMods;
import com.sun.codemodel.internal.JMod;

public class MetaModelToJava {
	
	public static void metaModelToJava(File dir, String pkg, MetaModel metaModel) {
		MetaModelToJava m2j = new MetaModelToJava(dir, pkg, metaModel);
		try {
			m2j.generate();
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (JClassAlreadyExistsException e) {
			throw new RuntimeException(e);
		}
	}

	private File dir;
	private String pkg;
	private MetaModel metaModel;
	private JCodeModel codeModel;


	private MetaModelToJava(File dir, String pkg, MetaModel metaModel) {
		this.dir = dir;
		this.pkg = pkg;
		this.metaModel = metaModel;
		this.codeModel = new JCodeModel();
	}
	
	private String qName(String className) {
		return pkg + "." + className;
	}

	private void generate() throws IOException, JClassAlreadyExistsException {
		for (Class klass: metaModel.classes) {
			JDefinedClass dc = codeModel._class(qName(klass.name));
			if (klass.isAbstract) {
				makeAbstract(dc);
			}
		}
		for (Class klass: metaModel.classes) {
			JDefinedClass current = codeModel._getClass(qName(klass.name));
			if (klass.parent != null) {
				JDefinedClass sup = codeModel._getClass(qName(klass.parent.type.name));
				current._extends(sup);
			}
			for (Field field: klass.fields) {
				addField(current, field);
			}
		}
		codeModel.build(dir);
	}

	private void makeAbstract(JDefinedClass dc) {
		
		/* 
		 * JCodeModel interface for modifiers is incomplete.
		 * This is the reason for this horrible hack to make
		 * classes abstract.
		 */
		
		java.lang.Class<? extends JDefinedClass> cls = dc.getClass();
		java.lang.reflect.Field classMods;
		try {
			classMods = cls.getDeclaredField("mods");
			classMods.setAccessible(true);
			JMods jmods = (JMods) classMods.get(dc);
			
			java.lang.reflect.Field modsMods = JMods.class.getDeclaredField("mods");
			modsMods.setAccessible(true);
			modsMods.set(jmods, JMod.ABSTRACT | JMod.PUBLIC);
			
			classMods.set(dc, jmods);
			
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (NoSuchFieldException e) {
			throw new RuntimeException(e);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	private void addField(JDefinedClass dc, Field field) {
		String name = field.name;
		int mods = Modifier.PUBLIC;
		
		Type type = field.type;
		if (field.mult instanceof Single) {
			if (type instanceof Str) {
				dc.field(mods, String.class, name);
			}
			else if (type instanceof Int) {
				dc.field(mods, Integer.class, name);
			}
			else if (type instanceof Bool) {
				dc.field(mods, Boolean.class, name);
			}
			else if (type instanceof Klass) {
				Class cls = ((Klass)type).klass;
				dc.field(mods, codeModel._getClass(qName(cls.name)), field.name);
			}
			else {
				throw new RuntimeException("Invalid type: " + type);
			}
		}
		else if (field.mult instanceof Mult) {
			JClass list = codeModel.ref(List.class);
			if (type instanceof Str) {
				dc.field(mods, list.narrow(String.class), name);
			}
			else if (type instanceof Int) {
				dc.field(mods, list.narrow(Integer.class), name);
			}
			else if (type instanceof Bool) {
				dc.field(mods, list.narrow(Boolean.class), name);
			}
			else if (type instanceof Klass) {
				Class cls = ((Klass)type).klass;
				JDefinedClass target = codeModel._getClass(qName(cls.name));
				dc.field(mods, list.narrow(target), field.name);
			}
			else {
				throw new RuntimeException("Invalid type: " + type);
			}
		}
		else {
			throw new RuntimeException("Invalid multiplicity: " + field.mult);
		}
	}


}

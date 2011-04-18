package javascripthost;

import org.mozilla.javascript.Scriptable;

public class Test implements Scriptable {

	@Override
	public void delete(String arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void delete(int arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public Object get(String arg0, Scriptable arg1) {
		// TODO Auto-generated method stub
		return "[" + arg0 + "]";
	}

	@Override
	public Object get(int arg0, Scriptable arg1) {
		// TODO Auto-generated method stub
		return "[" + arg0 + "]";
	}

	@Override
	public String getClassName() {
		// TODO Auto-generated method stub
		return "FOO";
	}

	@Override
	public Object getDefaultValue(Class<?> arg0) {
		// TODO Auto-generated method stub
		return "DEFAULT";
	}

	@Override
	public Object[] getIds() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Scriptable getParentScope() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Scriptable getPrototype() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean has(String arg0, Scriptable arg1) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean has(int arg0, Scriptable arg1) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean hasInstance(Scriptable arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void put(String arg0, Scriptable arg1, Object arg2) {
		// TODO Auto-generated method stub

	}

	@Override
	public void put(int arg0, Scriptable arg1, Object arg2) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setParentScope(Scriptable arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setPrototype(Scriptable arg0) {
		// TODO Auto-generated method stub

	}

}

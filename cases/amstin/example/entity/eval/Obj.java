package amstin.example.entity.eval;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import amstin.example.entity.Derived;
import amstin.example.entity.Entity;
import amstin.example.entity.Field;

public class Obj {

	private static final String ID_COL = "id";
	private Entity type;
	private Map<String, Object> table;
	private int id;
	private boolean saved;

	// TODO: inverses
	
	
	public Obj(Entity type) {
		this.type = type; 
		this.table = new HashMap<String, Object>();
		for (Field field: type.fields) {
			table.put(field.name, field.defaultValue());
		}
		this.id = -1;
	}
	
	public Entity getType() {
		return type;
	}

	public boolean isSaved() {
		return saved;
	}
	
	public boolean save(Connection conn) {
		if (isSaved()) {
			return false;
		}
		return doSave(conn);
	}

	private boolean doSave(Connection conn) {
		try {
			setSaved(true); // to prevent infinite recursion in case of cyclic dependencies.
			saveDependencies(conn);
			if (hasId()) {
				return update(conn);
			}
			return insert(conn);
		} catch (SQLException e) {
			setSaved(false);
			e.printStackTrace();
			return false;
		}
	}
	
	@SuppressWarnings("unchecked")
	private void saveDependencies(Connection conn) {
		for (Field field: getType().fields) {
			if (field.isAssoc() && !field.isMany()) {
				((Obj)lookup(field)).save(conn);
			}
			if (field.isAssoc() && field.isMany()) {
				for (Object o: ((Collection)lookup(field))) {
					((Obj)o).save(conn);
				}
			}
		}
	}

	private boolean hasId() {
		return id != -1;
	}
	
	private void setId(int id) {
		this.id = id;
	}

	private int getId() {
		return id;
	}

	private void setSaved(boolean b) {
		saved = b;
	}

	private String columnClause(List<Field> fields) {
		int numOfFields = fields.size();
		String q = "(";
		if (numOfFields > 0) {
			q += columnName(fields.get(0));
			for (int i = 1; i < numOfFields; i++) {
				Field field = fields.get(i);
				
				if (!field.isMany()) {
					q += ", " + columnName(field);
				}
			}
		}
		q += ")";
		return q;
	}
	
	private String columnName(Field field) {
		return field.name;
	}
	
	private String setClause(List<Field> fields) {
		int numOfFields = fields.size();
		String q = "set ";
		if (numOfFields > 0) {
			q += assignment(fields.get(0));
			for (int i = 1; i < numOfFields; i++) {
				Field field = fields.get(i);
				
				if (!field.isMany()) {
					q += ", " + assignment(field);
				}
			}
		}
		return q;
	}

	private String assignment(Field field) {
		return columnName(field) + " = ?";
	}


	
	private String valuesPlaceholder(List<Field> fields) {
		int numOfFields = fields.size();
		String q = "values (";
		if (numOfFields > 0) {
			q += "?";
			for (int i = 1; i < numOfFields; i++) {
				if (!fields.get(i).isMany()) {
					q += ", ?";
				}
			}
		}
		q += ")";
		return q;
	}
	
	private void setValues(PreparedStatement stat, List<Field> fields) throws SQLException {
		int i = 1;
		for (Field field: fields) {
			if (!field.isMany()) {
				Object val = lookup(field);
				if (field.isAssoc()) {
					stat.setInt(i, ((Obj)val).getId());
				}
				else {
					stat.setObject(i, val);
				}
				i++;
			}
		}
	}
	
	private boolean insert(Connection conn) throws SQLException {
		List<Field> fields = getType().fields;
		PreparedStatement stat = conn.prepareStatement("insert into table " 
				+ tableName() + " " + columnClause(fields) + " "
				+ valuesPlaceholder(fields) + ";");
		
		setValues(stat, fields);
		stat.executeUpdate();
		ResultSet keys = stat.getGeneratedKeys();
		
		if (keys.next()) {
			setId(keys.getInt(1));
		}
		else {
			throw new RuntimeException("Could not retrieve generated key from db.");
		}
		
		return insertManyFields(conn);
	}
	
	private int numOfColumns() {
		int count = 0;
		for (Field field: getType().fields) {
			if (!field.isMany()) {
				count++;
			}
		}
		return count;
	}
	
	private boolean update(Connection conn) throws SQLException {
		List<Field> fields = getType().fields;
		PreparedStatement stat = conn.prepareStatement("update table " 
				+ tableName() + " " + setClause(fields) +  " where " + ID_COL + " = ?;");
		
		setValues(stat, fields);
		stat.setInt(numOfColumns() + 1, getId());
		stat.executeUpdate();
		return updateManyFields(conn);
	}

	private boolean updateManyFields(Connection conn) throws SQLException {
		for (Field field: getType().fields) {
			if (field.isMany()) {
				deleteAssocs(conn, field);
				insertManyField(conn, field);
			}
		}
		return true;
		
	}

	private void deleteAssocs(Connection conn, Field field) throws SQLException {
		PreparedStatement stat = conn.prepareStatement("delete from " + manyAssocTableName(field) 
				+ " where " + ID_COL + " = " + getId() + ";");
		stat.executeUpdate();
	}

	private boolean insertManyFields(Connection conn) throws SQLException {
		for (Field field: getType().fields) {
			if (field.isMany()) {
				insertManyField(conn, field);
			}
		}
		return true;
	}

	private void insertManyField(Connection conn, Field field) throws SQLException {
		if (field.isOrdered()) {
			insertOrderedManyField(conn, field);
		}
		else {
			insertUnorderedManyField(conn, field);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void insertUnorderedManyField(Connection conn, Field field) throws SQLException {
		PreparedStatement stat = conn.prepareStatement("insert into "
				+ manyAssocTableName(field) + " values(?, ?);");
		stat.setInt(1, getId());
		for (Object elt: ((Collection)lookup(field))) {
			setFieldValue(field, stat, 2, elt);
			stat.addBatch();
		}
		stat.executeBatch();
	}

	private void setFieldValue(Field field, PreparedStatement stat, int col, Object elt)
			throws SQLException {
		if (field.isAssoc()) {
			stat.setInt(col, ((Obj)elt).getId());
		}
		else {
			stat.setObject(col, elt);
		}
	}

	@SuppressWarnings("unchecked")
	private void insertOrderedManyField(Connection conn, Field field) throws SQLException {
		PreparedStatement stat = conn.prepareStatement("insert into "
				+ manyAssocTableName(field) + " values(?, ?, ?);");
		stat.setInt(1, getId());
		int pos = 0;
		for (Object elt: ((List)lookup(field))) {
			stat.setInt(2, pos++);
			setFieldValue(field, stat, 3, elt);
			stat.addBatch();
		}
		stat.executeBatch();
	}

	private String manyAssocTableName(Field field) {
		return tableName() + "_" + field.name;
	}

	private String tableName() {
		return getType().name;
	}

	private Object lookup(Field field) {
		return table.get(field.name);
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
	public void insert(String name, Object value) {
		Field field = findField(name);
		checkIfMany(field, value);
		((Collection)table.get(field.name)).add(value);
	}
	
	@SuppressWarnings("unchecked")
	public void insertAt(String name, int pos, Object value) {
		Field field = findField(name);
		checkIfMany(field, value);
		if (!field.isOrdered()) {
			throw new RuntimeException("insertAt is not allowed on unordered collections");
		}
		((List)table.get(field.name)).add(pos, value);
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
		for (Field field: getType().fields) {
			if (name.equals(field.name)) {
				return field;
			}
		}
		throw new RuntimeException("No such field: " + name);
	}
	

}

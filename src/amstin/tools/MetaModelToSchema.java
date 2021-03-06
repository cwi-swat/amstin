package amstin.tools;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


import amstin.example.schema.Column;
import amstin.example.schema.Key;
import amstin.example.schema.Modifier;
import amstin.example.schema.NotNull;
import amstin.example.schema.Ref;
import amstin.example.schema.Schema;
import amstin.example.schema.Table;
import amstin.models.grammar.Grammar;
import amstin.models.grammar.parsing.cps.Parser;
import amstin.models.meta.Bool;
import amstin.models.meta.Class;
import amstin.models.meta.Field;
import amstin.models.meta.Int;
import amstin.models.meta.Klass;
import amstin.models.meta.MetaModel;
import amstin.models.meta.Real;
import amstin.models.meta.Star;
import amstin.models.meta.Str;

public class MetaModelToSchema {

	public static void main(String[] args) throws IOException {
		MetaModel mm = amstin.models.meta.Boot.instance;
		MetaModelToSchema x = new MetaModelToSchema(mm);
		Schema s = x.toSchema();
		Grammar schemaGrammar = Parser.parseGrammar(amstin.example.schema._Main.SCHEMA_MDG);
		Writer writer = new PrintWriter(System.out);
		ModelToString.unparse(schemaGrammar, s, writer);
		writer.flush();
	}
	
	
	public static Schema metaModelToSchema(MetaModel metaModel) {
		MetaModelToSchema mmts = new MetaModelToSchema(metaModel);
		return mmts.toSchema();
	}
	
	private MetaModel metaModel;
	private Map<Class, Class> subType;

	private MetaModelToSchema(MetaModel metaModel) {
		this.metaModel = metaModel;
		this.subType = new HashMap<Class, Class>();
		deriveSubType();
	}
	
	private void deriveSubType() {
		for (Class klass: metaModel.classes) {
			if (klass.parent != null) {
				subType.put(klass, klass.parent.type);
			}
		}
	}

	private List<Class> rootClasses() {
		List<Class> result = new ArrayList<Class>();
		for (Class klass: metaModel.classes) {
			if (klass.parent == null) {
				result.add(klass);
			}
		}
		return result;
	}


	private Schema toSchema() {
		Schema schema = new Schema();
		schema.tables = new ArrayList<Table>();
		List<Class> classes = rootClasses();
		for (Class klass: classes) {
			defineTable(schema, klass);
		}
		for (Class klass: classes) {
			Table table = tableForClass(schema, klass);
			addPrimaryKey(table);
			List<Field> allFields = new ArrayList<Field>();
			Set<Class> subClasses = subClassesOf(klass);
			if (subClasses.size() > 1) {
				addTypeColumn(table);
			}
			for (Class sub: subClasses) {
				allFields.addAll(sub.fields);
			}
			addColumns(schema, allFields, table);
		}
		return schema;
	}
	
	private static String typeColumnName() {
		return "TYPE_NAME";
	}

	private void addTypeColumn(Table table) {
		Column cls = new Column();
		cls.name = typeColumnName();
		amstin.example.schema.VarChar varChar = new amstin.example.schema.VarChar();
		varChar.length = 256;
		cls.type = varChar;
		cls.modifiers = new ArrayList<Modifier>();
		cls.modifiers.add(new NotNull());
		table.columns.add(cls);		
	}

	private void addPrimaryKey(Table table) {
		Column key = new Column();
		key.name = primaryKeyName();
		key.type = new amstin.example.schema.Int();
		key.modifiers = new ArrayList<Modifier>();
		key.modifiers.add(new Key());
		table.columns.add(key);
	}

	
	private Table tableForClass(Schema schema, Class klass) {
		klass = rootClassOf(klass);
		
		for (Table table: schema.tables) {
			if (tableName(klass).equals(table.name)) {
				return table;
			}
		}
		return null;
	}
	
	private Set<Class> subClassesOf(Class klass) {
		Set<Class> result = new HashSet<Class>();
		List<Class> todo = new ArrayList<Class>();
		todo.add(klass);
		while (!todo.isEmpty()) {
			Class cur = todo.remove(0);
			result.add(cur);
			for (Map.Entry<Class, Class> entry: subType.entrySet()) {
				if (entry.getValue().equals(cur)) {
					todo.add(entry.getKey());
				}
			}
		}
		return result;
	}

	private Class rootClassOf(Class klass) {
		Class current = klass;
		Class prev = null;
		while (current != null) {
			prev = current;
			current = subType.get(current);
		}
		return prev;
	}

	private static Table defineTable(Schema schema, String name) {
		Table table = new Table();
		table.name = name;
		table.columns = new ArrayList<Column>();
		schema.tables.add(table);
		return table;
	}
	
	private static Table defineTable(Schema schema, Class klass) {
		return defineTable(schema, klass.name);
	}

	private void addColumns(Schema schema, List<Field> fields, Table table) {
		for (Field field: fields) {
			
			if (field.mult instanceof Star) {
				defineJoinTableForList(schema, table, field);
				continue;
			}
		
			Column col = new Column();
			col.modifiers = new ArrayList<Modifier>();
			col.name = field.name;
			
			
			
			if (field.type instanceof Str) {
				amstin.example.schema.VarChar varChar = new amstin.example.schema.VarChar();
				varChar.length = 256;
				col.type = varChar;
			}
			else if (field.type instanceof Int) {
				col.type = new amstin.example.schema.Int();				
			}
			else if (field.type instanceof Real) {
				col.type = new amstin.example.schema.Real();								
			}
			else if (field.type instanceof Bool) {
				col.type = new amstin.example.schema.Bool();				
			}
			else if (field.type instanceof Klass) {
				makeForeignKey(schema, field, col);
				defineJoinTable(schema, table, field);
			}
			else {
				throw new RuntimeException("Invalid type: " + field.type);
			}
			
			table.columns.add(col);
		}
	}
	
	private static String joinFieldName(Table table, Field field) {
		Class klass = ((Klass)field.type).klass;
		return table.name + "_" + field.name + "_" + klass.name;
	}

	private void defineJoinTable(Schema schema, Table table, Field field) {
		Class klass = ((Klass)field.type).klass;
		String name = joinFieldName(table, field);
		for (Table t: schema.tables) {
			if (name.equals(t.name)) {
				throw new RuntimeException("Join table " + name + " already exists!");
			}
		}
		Table joinTable = defineTable(schema, name);
		Column col1 = new Column();
		col1.name = foreignKeyName(table.name);
		col1.type = new amstin.example.schema.Int();
		col1.modifiers = new ArrayList<Modifier>();
		col1.modifiers.add(new NotNull());
		Ref ref = new Ref();
		ref.table = table;
		col1.modifiers.add(ref);
		
		Column col2 = new Column();
		col2.name = columnName(field);
		col2.type = new amstin.example.schema.Int();
		col2.modifiers = new ArrayList<Modifier>();
		col2.modifiers.add(new NotNull());
		
		ref = new Ref();
		ref.table = tableForClass(schema, klass);
		col2.modifiers.add(ref);
		
		joinTable.columns.add(col1);
		joinTable.columns.add(col2);
	}

	private void defineJoinTableForList(Schema schema, Table table, Field field) {
		String name = joinFieldName(table, field);
		for (Table t: schema.tables) {
			if (name.equals(t.name)) {
				throw new RuntimeException("Join table " + name + " already exists!");
			}
		}
		Table joinTable = defineTable(schema, name);
		
		Column col1 = new Column();
		col1.name = foreignKeyName(table.name);
		col1.type = new amstin.example.schema.Int();
		col1.modifiers = new ArrayList<amstin.example.schema.Modifier>();
		col1.modifiers.add(new amstin.example.schema.NotNull());
		amstin.example.schema.Ref ref = new amstin.example.schema.Ref();
		ref.table = table;
		col1.modifiers.add(ref);
		
		Column col2 = new Column();
		col2.name = "position";
		col2.type = new amstin.example.schema.Int();
		col2.modifiers = new ArrayList<amstin.example.schema.Modifier>();
		col2.modifiers.add(new amstin.example.schema.NotNull());
		
		Column col3 = new Column();
		col3.modifiers = new ArrayList<Modifier>();
		col3.name = columnName(field);

		
		if (field.type instanceof Str) {
			amstin.example.schema.VarChar varChar = new amstin.example.schema.VarChar();
			varChar.length = 256;
			col3.type = varChar;
		}
		else if (field.type instanceof Int) {
			col3.type = new amstin.example.schema.Int();				
		}
		else if (field.type instanceof Real) {
			col3.type = new amstin.example.schema.Real();								
		}
		else if (field.type instanceof Bool) {
			col3.type = new amstin.example.schema.Bool();				
		}
		else if (field.type instanceof Klass) {
			makeForeignKey(schema, field, col3);
		}
		else {
			throw new RuntimeException("Invalid type: " + field.type);
		}
		
		
		joinTable.columns.add(col1);
		joinTable.columns.add(col2);
		joinTable.columns.add(col3);
		
	}

	private void makeForeignKey(Schema schema, Field field, Column col) {
		amstin.example.schema.Ref ref;
		col.type = new amstin.example.schema.Int();
		ref = new amstin.example.schema.Ref();
		ref.table = tableForClass(schema, ((Klass)field.type).klass);
		col.modifiers.add(ref);
	}
	
	
	private String columnName(Field field) {
		if (field.type instanceof Klass) {
			return foreignKeyName(field.name);
		}
		if (field.name.equals(primaryKeyName())) {
			throw new RuntimeException("Cannot use " + primaryKeyName() + " as class name");
		}
		return field.name;
	}
	
	private String tableName(Class klass) {
		return klass.name;
	}
	
	private String foreignKeyName(String name) {
		return name + "_id";
	}
	
	private String primaryKeyName() {
		return "ID";
	}
	
	
	
	
}

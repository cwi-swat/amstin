package amstin.tools;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import amstin.models.grammar.Grammar;
import amstin.models.grammar.parsing.Parser;
import amstin.models.meta.Bool;
import amstin.models.meta.Class;
import amstin.models.meta.Field;
import amstin.models.meta.Int;
import amstin.models.meta.Klass;
import amstin.models.meta.MetaModel;
import amstin.models.meta.Real;
import amstin.models.meta.Star;
import amstin.models.meta.Str;
import amstin.models.schema.Column;
import amstin.models.schema.Modifier;
import amstin.models.schema.NotNull;
import amstin.models.schema.Schema;
import amstin.models.schema.Table;

public class MetaModelToSchema {

	public static void main(String[] args) throws IOException {
		MetaModel mm = amstin.models.meta.Boot.instance;
		MetaModelToSchema x = new MetaModelToSchema(mm);
		Schema s = x.toSchema();
		Grammar schemaGrammar = Parser.parseGrammar(amstin.models.schema._Main.SCHEMA_MDG);
		Writer writer = new PrintWriter(System.out);
		ModelToString.unparse(schemaGrammar, s, writer);
		writer.flush();
	}
	
	
	private MetaModel metaModel;

	private MetaModelToSchema(MetaModel metaModel) {
		this.metaModel = metaModel;
	}
	
	private Schema toSchema() {
		Schema schema = new Schema();
		schema.tables = new ArrayList<Table>();
		for (Class klass: metaModel.classes) {
			defineTable(schema, klass);
		}
		for (Class klass: metaModel.classes) {
			addColumns(schema, klass.fields, tableForClass(schema, klass));
		}
		return schema;
	}

	
	private Table tableForClass(Schema schema, Class klass) {
		for (Table table: schema.tables) {
			if (tableName(klass).equals(table.name)) {
				return table;
			}
		}
		return null;
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
				amstin.models.schema.VarChar varChar = new amstin.models.schema.VarChar();
				varChar.length = 256;
				col.type = varChar;
			}
			else if (field.type instanceof Int) {
				col.type = new amstin.models.schema.Int();				
			}
			else if (field.type instanceof Real) {
				col.type = new amstin.models.schema.Real();								
			}
			else if (field.type instanceof Bool) {
				col.type = new amstin.models.schema.Bool();				
			}
			else if (field.type instanceof Klass) {
				col.type = new amstin.models.schema.Int();
				defineJoinTable(schema, table, field);
			}
			else {
				throw new RuntimeException("Invalid type: " + field.type);
			}
		}
	}

	private void defineJoinTable(Schema schema, Table table, Field field) {
		Class klass = ((Klass)field.type).klass;
		String name = table.name + "_" + field.name + "_" + klass.name;
		for (Table t: schema.tables) {
			if (name.equals(t.name)) {
				throw new RuntimeException("Join table " + name + " already exists!");
			}
		}
		Table joinTable = defineTable(schema, name);
		Column col1 = new Column();
		col1.name = foreignKeyName(table.name);
		col1.type = new amstin.models.schema.Int();
		col1.modifiers = new ArrayList<Modifier>();
		col1.modifiers.add(new NotNull());
		
		Column col2 = new Column();
		col2.name = columnName(field);
		col2.type = new amstin.models.schema.Int();
		col2.modifiers = new ArrayList<Modifier>();
		col2.modifiers.add(new NotNull());
		
		table.columns.add(col1);
		table.columns.add(col2);
		schema.tables.add(joinTable);
	}

	private void defineJoinTableForList(Schema schema, Table table, Field field) {
		Class klass = ((Klass)field.type).klass;
		String name = table.name + "_" + field.name + "_" + klass.name;
		for (Table t: schema.tables) {
			if (name.equals(t.name)) {
				throw new RuntimeException("Join table " + name + " already exists!");
			}
		}
		Table joinTable = defineTable(schema, name);
		
		Column col1 = new Column();
		col1.name = foreignKeyName(table.name);
		col1.type = new amstin.models.schema.Int();
		col1.modifiers = new ArrayList<amstin.models.schema.Modifier>();
		col1.modifiers.add(new amstin.models.schema.NotNull());
		amstin.models.schema.Ref ref = new amstin.models.schema.Ref();
		ref.table = table;
		col1.modifiers.add(ref);
		
		Column col2 = new Column();
		col2.name = "position";
		col2.type = new amstin.models.schema.Int();
		col2.modifiers = new ArrayList<amstin.models.schema.Modifier>();
		col2.modifiers.add(new amstin.models.schema.NotNull());
		
		Column col3 = new Column();
		col3.modifiers = new ArrayList<Modifier>();
		col3.name = columnName(field);

		
		if (field.type instanceof Str) {
			amstin.models.schema.VarChar varChar = new amstin.models.schema.VarChar();
			varChar.length = 256;
			col3.type = varChar;
		}
		else if (field.type instanceof Int) {
			col3.type = new amstin.models.schema.Int();				
		}
		else if (field.type instanceof Real) {
			col3.type = new amstin.models.schema.Real();								
		}
		else if (field.type instanceof Bool) {
			col3.type = new amstin.models.schema.Bool();				
		}
		else if (field.type instanceof Klass) {
			col3.type = new amstin.models.schema.Int();
			ref = new amstin.models.schema.Ref();
			ref.table = tableForClass(schema, ((Klass)field.type).klass);
			col3.modifiers.add(ref);
		}
		else {
			throw new RuntimeException("Invalid type: " + field.type);
		}
		
		
		table.columns.add(col1);
		table.columns.add(col2);
		table.columns.add(col3);
		schema.tables.add(joinTable);
		
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
		return "id";
	}
	
	
	
	
}

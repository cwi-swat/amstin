
require 'schema/schemaschema'
require 'tools/copy'
require 'tools/print'
require 'schema/factory'

newSchema = Copy.new(Factory.new(SchemaSchema.schema)).copy(SchemaSchema.schema)

Print.recurse(newSchema, SchemaSchema.print_paths)

puts "WOA: #{newSchema.classes['Klass'].schema.name}"
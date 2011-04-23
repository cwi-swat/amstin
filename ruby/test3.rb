
require 'test2'
require 'tools/copy'
require 'schema/factory'

newSchema = Copy.new(Factory.new(SchemaSchema.schema)).copy(SchemaSchema.schema)

PrintSchema.new(newSchema).run

puts "WOA: #{newSchema.classes['Klass'].schema.name}"
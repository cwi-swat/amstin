
require 'test2'
require 'tools/copy'
require 'schema/factory'

PrintSchema.run(SchemaSchema.schema)

newSchema = Copy.new(Factory.new(SchemaSchema.schema)).copy(SchemaSchema.schema)

PrintSchema.run(newSchema)

puts "WOA: #{newSchema.classes['Klass'].schema.name}"
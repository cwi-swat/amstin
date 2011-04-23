
require 'schema/schemaschema.rb'
require 'schema/factory'

f = Factory.new(SchemaSchema.schema)

s = f.Schema
s.name = 'foo'

begin
  s.name = 3
  puts "There is a problem"
rescue
end

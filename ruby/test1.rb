
require 'schema/schemaschema'
require 'schema/checkschema'

if __FILE__ == $0 then
  
  # if you want to print something out, see example at end of print.rb 
  ss = SchemaSchema.schema
  puts "Checking #{ss.name}"

  check = Conformance.new
  check.recurse(ss.classes["Schema"], ss)
  check.errors.each do |x|
    puts x
  end
end


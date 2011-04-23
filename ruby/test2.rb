
require 'schema/schemaschema'
require 'cyclicmap'

class PrintSchema < CyclicApply

  def Schema(o)
    puts o
    puts "SCHEMA #{o.name}"
    o.classes.each {|x| recurse(x)}
  end

  def Klass(o)
    puts "  CLASS #{o.name}"
    o.fields.each {|x| recurse(x)}
  end

  def Field(o)
    puts "    FIELD #{o.name}"
  end
end

if __FILE__ == $0 then
  PrintSchema.new(SchemaSchema.schema).run
  
  require 'grammar/grammarschema'
  
  PrintSchema.new(GrammarSchema.schema).run
end

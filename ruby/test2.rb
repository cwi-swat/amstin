
require 'schemagen.rb'
require 'cyclicmap.rb'

class PrintSchema < CyclicApply

  def Schema(o)
	puts "SCHEMA #{o.name}"
	o.classes.each {|x| recurse(x)}
  end

  def Klass(o)
	puts "  CLASS #{o.name}"
	o.fields.each {|x| recurse(x)}
  end

  def Field(o)
	puts "    FIELD #{o.name}"
	#o.classes.each(&:recurse)
  end
end

PrintSchema.new(SchemaSchema.schema).run

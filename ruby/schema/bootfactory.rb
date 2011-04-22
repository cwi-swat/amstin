
require 'schema/schemamodel'

class Factory
  # TODO: the real factory should produce checked versions

  def initialize(schema)
    @schema = schema
  end

  def method_missing(sym, *args, &block)
    klass = @schema.classes.find do |c|
      c.name == sym.to_s
    end
    raise "No such class #{sym}" unless klass
    m = SchemaModel.new
    klass.fields.each_with_index do |f, i|
      #puts "FIELD: #{f.name}"
      #puts "Arg: #{args[i]}"
      m[f.name] = args[i]
    end
    return m
  end
end

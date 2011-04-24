
class Copy
  def initialize(factory)
    @factory = factory
    @memo = {}
  end

  def copy(source)
    return nil if source.nil?
    target = @memo[source]
    return target if target
    
    klass = source.schema_class
    raise "Source does not have a schema_class #{source}" unless klass
    target = @factory[klass.name]
    @memo[source] = target
    klass.fields.each do |field|
      #puts "Copying #{field.name} #{source[field.name].class} #{source[field.name]}"
      if {"int"=>1,"str"=>1,"bool"=>1}[field.type.name]
        target[field.name] = source[field.name]
      elsif !field.many
        target[field.name] = copy(source[field.name])
      else
        source[field.name].each do |x|
          target[field.name] << copy(x)
        end
      end
    end
    return target
  end
end


if __FILE__ == $0 then

  require 'schema/schemaschema'
  require 'tools/print'
  require 'schema/factory'
  
  newSchema = Copy.new(Factory.new(SchemaSchema.schema)).copy(SchemaSchema.schema)
  
  Print.recurse(newSchema, SchemaSchema.print_paths)
  
  puts "WOA: #{newSchema.classes['Klass'].schema.name}"

end
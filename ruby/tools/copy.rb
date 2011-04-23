
require 'cyclicmap'

class Copy
  def initialize(factory)
    @factory = factory
    @memo = {}
  end

  def copy(source)
    return nil if source.nil?
    target = @memo[source]
    return target if target
    
    klass = source.metaclass
    raise "Unknown class '#{m}'" unless klass
    target = @factory[klass.name]
    @memo[source] = target
    klass.fields.each do |field|
      #puts "Copying #{field.name} #{field.type.name}"
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


# This is a *generic* printing function, that can print a indented dump
# of any object. It uses the model to 

# obj: the object to be printed
# paths: this is s nested record type that guides the printing process.
#   the keys tell the printer which fields to traverse from the main object
#   the values are the paths for the subobject
# indent: amount of indent
# inverse: an internal argument that tells what field was just traveresed,
#   so that its inverse will not be printed on the subobjects. This just
#   cleans up the printout a little

class Print
  def self.recurse(obj, paths={}, indent=0, inverse=nil)
    klass = obj.schema_class   # TODO: pass as an argument for partial evaluation
    if obj.nil?
      puts "nil"
    else
      puts klass.name
      #puts "FOO #{obj} p=#{paths} i=#{inverse}"
      indent += 2
      klass.fields.each do |field|
        if field.type.schema_class.name == "Primitive"
          print " "*indent, field.name, ": ", obj[field.name], "\n"
        else
          sub_path = paths[field.name.to_sym]
          if sub_path
            if !field.many
              print " "*indent, field.name, " "
              recurse(obj[field.name], sub_path || {}, indent, field.name)
            else
              print " "*indent, field.name, "\n"
              indent += 2
              obj[field.name].each_with_index do |sub, i|
                print " "*indent, "#", i, " "
                recurse(sub, sub_path || {}, indent, field.name)
              end
            end
          elsif !field.many && key(field.type) && (!field.inverse || field.inverse.name != inverse)
            x = obj[field.name]
            print " "*indent, field.name, ": ", (x.nil?) ? "nil" : x[key(field.type).name], "\n"
          end
        end
      end
    end
  end
  
  def self.key(type)
    type.fields.find { |f| f.key && f.type.schema_class.name == "Primitive" }
  end  

end

if __FILE__ == $0 then
  require 'schema/schemaschema'
  # Print.recurse(SchemaSchema.schema) # this also works
   
  Print.recurse(SchemaSchema.schema, SchemaSchema.print_paths)
  
  require 'grammar/grammarschema'  
  Print.recurse(GrammarSchema.schema, SchemaSchema.print_paths)
end

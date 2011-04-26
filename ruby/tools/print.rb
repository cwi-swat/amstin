
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
  def self.recurse(obj, paths={}, indent=0, visited=[])
    if obj.nil?
      puts "nil"
    else
      visited.push obj
      klass = obj.schema_class   # TODO: pass as an argument for partial evaluation
      puts klass.name
      #puts "FOO #{obj} p=#{paths} i=#{visited}"
      indent += 2
      klass.fields.each do |field|
        if field.type.schema_class.name == "Primitive"
          print " "*indent, field.name, ": ", obj[field.name], "\n"
        else
          sub_path = paths[field.name.to_sym]
          if sub_path
            if !field.many
              print " "*indent, field.name, " "
              sub = obj[field.name]
              recurse(sub, sub_path || {}, indent, visited)
            else
              print " "*indent, field.name, "\n"
              subindent = indent + 2
              obj[field.name].each_with_index do |sub, i|
                print " "*subindent, "#", i, " "
                recurse(sub, sub_path || {}, subindent, visited)
              end
            end
          elsif !field.many && !visited.include?(obj[field.name])
            #puts "CHECK #{obj[field.name]} #{visited.to_s}"
            sub = obj[field.name]
            print " "*indent, field.name, ": "
            if sub.nil?
              print "nil\n"
            elsif key(sub.schema_class)  
              # TODO: annoying that we need to know actual type, not just declared type
              # This is because we don't have field inheritance in the base schema
              print sub[key(sub.schema_class).name], "\n"
            else
              recurse(sub, {}, indent, visited)
            end
          end
        end
      end
      visited.pop
    end
  end
  
  def self.key(klass)
    klass.fields.find { |f| f.key && f.type.schema_class.name == "Primitive" }
  end  

end

if __FILE__ == $0 then
  require 'schema/schemaschema'
  # Print.recurse(SchemaSchema.schema) # this also works
   
  Print.recurse(SchemaSchema.schema, SchemaSchema.print_paths)
  
  require 'grammar/grammarschema'  
  Print.recurse(GrammarSchema.schema, SchemaSchema.print_paths)
end

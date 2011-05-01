require 'cyclicmap'
require 'tools/diff'
require 'tools/copy'

class Identify < MemoBase
  def self.placeholder_char
    "_"
  end
  
  def initialize(mapping, source, target)
    super()
    @mapping = mapping
    @source_root = source
    @target_root = target
    @mapping[source] = target 
  end

  def identify(obj)
    return if obj.nil? || @memo[obj]
    @memo[obj] = true
    obj.schema_class.fields.each do |f|
      Field(f, obj)
    end
  end

  def Field(field, obj)
    if field.type.schema_class.name == "Primitive"
      if field.key
        if obj[field.name][0] == Identify.placeholder_char
          analog = lookup_object(obj)
          @mapping[obj] = analog
          #puts "MAP #{obj} to #{analog}"
        end
      end
    else
      if !field.many
        x = obj[field.name]
        identify(x)
      else
        obj[field.name].each do |x|
          identify(x)
        end
      end
    end  
  end

  # find an object with an equivalent name  
  def lookup_object(obj)
    raise "Keys cannot be null" if obj.nil?
    rel_key_field = SchemaSchema.keyRel(obj.schema_class)
    if rel_key_field.nil?
      raise "Keys should connect to root but stop at #{obj}" if obj != @source_root
      return @target_root
    else
      key_field = SchemaSchema.key(obj.schema_class)

      raise "Key relationship fields must have inverses" if rel_key_field.inverse.nil?
      raise "A relationship key must have a data key as well" if key_field.nil?

      base = lookup_object(obj[rel_key_field.name])
      key = obj[key_field.name][1..-1]
      #puts "IDENTIFY #{key_field.name}/#{rel_key_field.name} key #{key}"
      return base[rel_key_field.inverse.name][key]
    end
  end
end

class Merge < DiffBase
  def initialize()
    super()
  end

  def merge(from, to, factory)
    # adds identifications to the memo table
    @identity = {}

    id = Identify.new(@identity, from, to)
    id.identify(from)

    @copier = Copy.new(factory, @identity)
    
    diff(from, to)
    to.finalize
    to
  end

  # just insert everything from the left
  def ordered(field, o1, o2) 
    o1[field.name].each do |left|
      different_insert(o2, field, left)
    end
  end

  def keyed(field, o1, o2)
    #puts "KEY #{field} #{o1[field.name]} #{o2[field.name]}"
    o1[field.name].keys.each do |key_val|
      left = o1[field.name][key_val]
      if key_val[0] == Identify.placeholder_char
        right = o2[field.name][key_val[1..-1]]
        raise "could not find object named #{key_val}" if right.nil?
        Type(field.type, left, right)
      else
        right = o2[field.name][key_val]
        raise "attempt to overwrite object named #{key_val}" unless right.nil?
        different_insert(o2, field, left)
      end
    end
  end

  def different_single(target, field, old, new)
    return if new.nil?
    if field.type.schema_class.name == "Primitive"
      return if field.key && new[0] == Identify.placeholder_char
      target[field.name] = new
      #puts "SET #{target}.#{field.name} = #{new}"
    else
      raise "Merge cannot change single-valued field #{target}.#{field.name} from #{old} to #{new}"
    end
  end

  def different_insert(target, field, new)
    #puts "COPYING #{target[field.name]}.#{field.name} #{new}"
    target[field.name] << @copier.copy(new)
  end
  
  def different_delete(target, field, old)
    raise "Merge cannot delete from #{target}.#{field.name}"
  end
end

require 'grammar/cpsparser'
require 'grammar/grammargrammar'
require 'tools/print'

g1 = CPSParser.load_raw('grammar/grammar.grammar', GrammarGrammar.grammar, GrammarSchema.schema)
g2 = CPSParser.load_raw('grammar/pretty_grammar.grammar', GrammarGrammar.grammar, GrammarSchema.schema)

#Print.new.recurse(g2, GrammarSchema.print_paths)

puts "FOOBAR #{g1._graph_id}"
g1p2 = Merge.new.merge(g2, g1, g1._graph_id)

Print.new.recurse(g1p2, GrammarSchema.print_paths)

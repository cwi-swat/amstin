class Finalize < MemoBase
  def initialize()
    super()
    @indent = 0
  end

  def finalize(obj)
    return if obj.nil? || @memo[obj]
    #puts " "*@indent + "FINALIZE #{obj}"
    @indent += 1    
    @memo[obj] = true
    klass = obj.schema_class
    klass.fields.each do |f|
      Field(f, obj)
    end
    @indent -= 1
  end
  
  def Field(field, obj)
    val = obj[field.name]

    if field.optional
      return if val.nil?
    else
      if !field.many ? val.nil? : val.empty?
        raise "Field #{field.name} is required" 
      end
    end

    return if field.type.schema_class.name == "Primitive"

    #puts " "*@indent + "CHECKING #{obj}.#{field.name}:'#{val}'"
    @indent += 1

    # update delayed inverses
    if field.inverse && field.inverse.many
      #puts " "*@indent + "INVERTED #{field.inverse.name}:'#{val[field.inverse.name]}'"
      _each(field, val) do |val|
        if !val[field.inverse.name].include?(obj)
          #puts " "*@indent + "FIXING #{obj}"
          val[field.inverse.name] << obj
        end
      end
    end

    # check the field values    
    _each(field, val) do |val|
      finalize(val)
    end
    @indent -= 1
  end  

  def _each(field, val)
    if !field.many
      yield val
    else
      val.each do |x|
        yield x
      end
    end
  end
end

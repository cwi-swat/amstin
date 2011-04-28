

require 'cyclicmap'

class Equals < CyclicThing

  def recurse(this, o1, o2)
    if @memo[[this, o1, o2]] then
      return true
    end
    @memo[[this, o1, o2]] = true
    #puts "Sending to: #{this.schema_class.name}"
    x = send(this.schema_class.name, this, o1, o2)
    unless x then
      #puts "Not equal: #{this}, #{o1} != #{o2}"
    end
    return x
  end

  def run(schema, o1, o2)
    recurse(schema, o1, o2)
  end

  def Schema(this, o1, o2)
    #puts o1.schema_class
    #puts o2.schema_class
    return false unless o1.schema_class == o2.schema_class
    recurse(o1.schema_class, o1, o2)
  end

  def Primitive(this, o1, o2)
    o1 == o2
  end

  def Klass(this, o1, o2)
    #puts "KLASS: #{this}, #{o1}, #{o2}"
    this.fields.inject(true) do |eq, f|
      eq && recurse(f, o1, o2)
    end
  end

  def Field(this, o1, o2)
    # o1 and o2 are the owners
    #puts "FIELD: #{this.name}, #{o1} ==? #{o2}"
    #puts this.many
    if this.many then
      return false unless o1[this.name].length == o2[this.name].length
      each2 = o2[this.name].each
      o1[this.name].each do |x|
        # what if they are unordered collections?
        return false unless recurse(this.type, x, each2.next)
      end
    end
    return false if o1[this.name].nil? && !o2[this.name].nil?
    return false if !o1[this.name].nil? && o2[this.name].nil?

    if !o1[this.name].nil? then
      return recurse(this.type, o1[this.name], o2[this.name])
    end
    return true
    # should we check inverses?
  end

end

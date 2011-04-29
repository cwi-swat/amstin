

require 'cyclicmap'

class Equals < MemoBase

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

  def self.equals(schema, o1, o2)
    self.new.recurse(schema, o1, o2)
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

  def unordered(this, o1, o2)
    puts "Unordered comparison"
    o1[this.name].each do |x|
      found = false
      o2[this.name].each do |y|
        found = recurse(this.type, x, y)
        break if found
      end
      return false unless found
    end
    return true
  end

  def ordered(this, o1, o2) 
    each2 = o2[this.name].each
    o1[this.name].each do |x|
      return false unless recurse(this.type, x, each2.next)
    end
  end

  def many(this, o1, o2)
    return false unless o1[this.name].length == o2[this.name].length
    #if this.inverse then 
    unordered(this, o1, o2)
    #else
    #  ordered(this, o1, o2)
    #end
  end

  def single(this, o1, o2)
    return false unless o1[this.name].nil? == o2[this.name].nil?
    return true if o1[this.name].nil?
    recurse(this.type, o1[this.name], o2[this.name])
  end

  def Field(this, o1, o2)
    # o1 and o2 are the owners
    if this.many then
      many(this, o1, o2)
    else
      single(this, o1, o2)
    end
  end

end

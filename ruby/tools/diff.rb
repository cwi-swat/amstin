

require 'cyclicmap'

class Diff < MemoBase
  def initialize()
    super()
    @diffs = []
  end

  def self.diff(o1, o2)
    self.new.diff(o1, o2)
  end
  
  def diff(o1, o2)
    Klass(o1.schema_class, o1, o2)
    #@diffs.each do |d| puts "DIFF #{d}" end
    @diffs
  end

  def Type(this, o1, o2)
    #puts "#{this.class} #{this.name} #{o1} #{o2}"
    return send(this.schema_class.name, this, o1, o2)
  end

  def Primitive(this, o1, o2)
    return o1 == o2
  end

  def Klass(this, o1, o2)
    if o1.nil? || o2.nil?
      return o1.nil? == o2.nil?
    end
    existing = @memo[o1]
    if existing
      return existing == o2
    end
    @memo[o1] = o2
    this.fields.each do |f|
      Field(f, o1, o2)
    end
    return true # diffs have already been accounted for
  end

  def Field(field, o1, o2)
    # o1 and o2 are the owners
    if field.many then
      many(field, o1, o2)
    else
      single(field, o1, o2)
    end
  end

  def single(field, o1, o2)
    if !Type(field.type, o1[field.name], o2[field.name])
      @diffs << [:set, o1, field.name, o1[field.name]]
    end
  end

  def many(field, o1, o2)
    if SchemaSchema.key(field.type) then 
      keyed(field, o1, o2)
    else
      ordered(field, o1, o2)
    end
  end

  def ordered(field, o1, o2) 
    o1[field.name].zip(o2[field.name]).each do |left, right|
      if left.nil?
        @diffs << [:insert, o1, field.name, right]
      elsif right.nil?
        @diffs << [:delete, o1, field.name, left]
      else
        Type(field.type, left, right)
      end
    end
  end

  def keyed(field, o1, o2)
    key = SchemaSchema.key(field.type)
    o1[field.name].each do |left|
      key_val = left[key.name]
      right = o2[field.name][key_val]
      if right
        Type(field.type, left, right)
      else
        @diffs << [:delete_key, o1, field.name, key_val]
      end
    end
    o2[field.name].each do |right|
      key_val = right[key.name]
      left = o1[field.name][key_val]
      if left.nil?
        @diffs << [:insert, o1, field.name, right]
      end
    end
  end

end



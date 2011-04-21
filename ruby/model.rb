

class BootstrapModel < BasicObject
  def initialize(klass = nil)
    @fields = {}
    @fields[:klass] = klass
  end

  def method_missing(name, *args, &block)
    if (name.to_s =~ /^([a-zA-Z0-9]*)=$/)
      @fields[$1.to_sym] = args[0]
    elsif @fields[name]
      @fields[name]
    else
      super(name, *args, &block)
    end
  end

  def to_s
    "model(#{self.klass})"
  end

end

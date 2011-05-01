
require 'grammar/cpsparser'
require 'grammar/grammargrammar'
require 'tools/print'

require 'wx'
include Wx


def DiagramEdit()

  frame = Frame.new(nil, :title => 'So far, so good...')
  frame.show

end

class DiagramFrame < Wx::Frame
   def initialize(shape)
     super(nil, :title => 'Diagram')
     evt_paint :on_paint
     evt_left_down :on_mouse_down
     evt_motion :on_move
     evt_left_up :on_mouse_up
     @shape = shape
     @down = false
   end

   def on_mouse_down(e)
     @down = true
     @down_x = e.x
     @down_y = e.y
   end
   
   def on_mouse_up(e)
     @down = false
   end

   def on_move(e)
     return unless @down
     @shape.position.x += e.x - @down_x
     @shape.position.y += e.y - @down_y
     on_mouse_down(e)
     refresh()
   end

   # Writes the gruff graph to a file then reads it back to draw it
   def on_paint
     paint do | dc |
        Shape(dc, @shape)
     end
   end
   
   def Color(c)
     Wx::Colour.new(c.r, c.g, c.b)
   end

   def LineFormat(lf)
     Wx::Pen.new(Color(lf.color), lf.width) # style!!!
   end
      
   def ShapeFormat(dc, sf)
      dc.set_pen(LineFormat(sf.line))
      dc.set_brush(Wx::Brush.new(Color(sf.fill_color)))
   end
   
   def Shape(dc, shape)
     ShapeFormat(dc, shape.format)
     r = shape.position
     dc.draw_rectangle(r.x, r.y, r.w, r.h)
     #content
   end
   
   
end

grammar_grammar = GrammarGrammar.grammar
schema_grammar = CPSParser.load('schema/schema.grammar', grammar_grammar, GrammarSchema.schema)

diagram_schema = CPSParser.load('applications/diagramedit/diagram.schema', schema_grammar, SchemaSchema.schema)

f = Factory.new(diagram_schema)

red = f.Color(100, 0, 0)
black = f.Color(0, 0, 0)
white = f.Color(100, 100, 100)
text = f.Text("Hello World", "Helvetica", 18, true, true, black)
shape = f.Shape(f.Rect(10, 10, 100, 100), text, f.ShapeFormat(f.LineFormat(5, "", red), white))
  
shape.finalize

Print.print(shape)

Wx::App.run { DiagramFrame.new(shape).show }


require 'grammar/cpsparser'
require 'grammar/grammargrammar'
require 'tools/print'

require 'wx'
include Wx

class DiagramFrame < Wx::Frame
   def initialize(part)
     super(nil, :title => 'Diagram')
     evt_paint :on_paint
     evt_left_down :on_mouse_down
     evt_motion :on_move
     evt_left_up :on_mouse_up
     @part = part

     @down = false
     @selection = nil
   end

   def on_mouse_down(e)
     @down = true
     @selection = nil
     @down_x = e.x
     @down_y = e.y
     find(@part, e)
   end
   
   def on_mouse_up(e)
     @down = false
   end

   def on_move(e)
     return unless @selection && @down
     @selection.boundary.x += e.x - @down_x
     @selection.boundary.y += e.y - @down_y
     @down_x = e.x
     @down_y = e.y
     refresh()
   end

   def find(part, pnt)
     catch :found do
       find1(part, pnt)
     end
   end
     
   def find1(part, pnt)
     if part.Container?
       part.items.each do |s|
         find1(s, pnt)
       end
     else
       if rect_contains(part.boundary, pnt)
         @selection = part
         refresh()
         throw :found
       end
     end
   end
   
   def rect_contains(rect, pnt)
     rect.x <= pnt.x && pnt.x <= rect.x + rect.w \
     && rect.y <= pnt.y && pnt.y <= rect.y + rect.h
   end
     
   # Writes the gruff graph to a file then reads it back to draw it
   def on_paint
     paint do | dc |
        drawPart(dc, @part)
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

   def drawPart(dc, part)
     if part.Shape?
       drawShape(dc, part)
     else
       (part.items.length-1).downto(0).each do |i|
         drawPart(dc, part.items[i])
       end
     end
   end
   
   def drawShape(dc, shape)
     ShapeFormat(dc, shape.format)
     r = shape.boundary
     dc.draw_rectangle(r.x, r.y, r.w, r.h)
   end
   
   
end

grammar_grammar = GrammarGrammar.grammar
schema_grammar = CPSParser.load('schema/schema.grammar', grammar_grammar, GrammarSchema.schema)

diagram_schema = CPSParser.load('applications/diagramedit/diagram.schema', schema_grammar, SchemaSchema.schema)

f = Factory.new(diagram_schema)

red = f.Color(255, 0, 0)
blue = f.Color(0, 0, 255)
black = f.Color(0, 0, 0)
white = f.Color(255, 255, 255)
text = f.Text(nil, "Hello World", "Helvetica", 18, true, true, black)
s1 = f.Shape(f.Rect(10, 10, 100, 100), f.ShapeFormat(f.LineFormat(5, "", red), white), text)
s2 = f.Shape(f.Rect(20, 20, 200, 100), f.ShapeFormat(f.LineFormat(10, "", blue), white), text)
content = f.Container(nil, 1, [s1, s2])

Print.print(content)

Wx::App.run { DiagramFrame.new(content).show }

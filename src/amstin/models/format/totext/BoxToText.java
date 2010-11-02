package amstin.models.format.totext;

import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import amstin.models.format.Align;
import amstin.models.format.Box;
import amstin.models.format.Cells;
import amstin.models.format.Group;
import amstin.models.format.Gs;
import amstin.models.format.Horizontal;
import amstin.models.format.Hs;
import amstin.models.format.Indented;
import amstin.models.format.Is;
import amstin.models.format.Op;
import amstin.models.format.Option;
import amstin.models.format.Row;
import amstin.models.format.Sep;
import amstin.models.format.SepList;
import amstin.models.format.Text;
import amstin.models.format.Vertical;
import amstin.models.format.Vs;

public class BoxToText {

	public static String boxToText(Box box) {
		return new BoxToText(box).toText();
	}

	private Box root;

	public BoxToText(Box box) {
		this.root = box;
	}

	private String toText() {
		StringWriter writer = new StringWriter();
		toText(root, writer, 0, true);
		return writer.toString();
	}

	private void toText(Box box, StringWriter writer, int indent, boolean mustIndent) {
		if (box instanceof Horizontal) {
			horizontalToText((Horizontal) box, writer, indent, mustIndent);
		}
		else if (box instanceof Vertical) {
			verticalToText((Vertical) box, writer, indent, mustIndent);
		}
		else if (box instanceof Indented) {
			indentedToText((Indented) box, writer, indent, mustIndent);
		}
		else if (box instanceof Text) {
			textToText((Text) box, writer, indent, mustIndent);
		}
		else if (box instanceof Align) {
			alignToText((Align)box, writer, indent, mustIndent);
		}
		else {
			throw new RuntimeException("unsupported box expression " + box.getClass());
		}
	}

	private String fillCol(String s, int width, char alignment) {
		switch (alignment) {
		case 'l': return String.format("%-" + width +  "s", s);
		case 'r': return String.format("%" + width +  "s", s);
		case 'c': 
			if (width > 1) { 
				return String.format("%" + (width / 2) +  "s%s%" + (width / 2) + "s", "", s, "");
			}
			return s;
		default: throw new RuntimeException("Invalid alignment: " + alignment);
		}
	}

	private void horizontalToText(Horizontal h, StringWriter writer, int indent, boolean mustIndent) {
		boolean first = true;
		for (Box k: each(h)) {
			if (!first && !isHorizontallyEmpty(k)) {
				writer.append(hfill(getHs(h.options)));
				toText(k, writer, indent, false);
			}
			else {
				toText(k, writer, indent, mustIndent);
			}
			first = false;
		}
	}

	private void verticalToText(Vertical v, StringWriter writer, int indent, boolean mustIndent) {
		boolean first = true;
		for (Box box: each(v)) {
			if (!first && !isVerticallyEmpty(box)) {
				writer.append(vfill(getVs(v.options)));
				toText(box, writer, indent, true);	
			}
			else {
				toText(box, writer, indent, mustIndent);
			}
			
			first = false;
		}
	}

	private void indentedToText(Indented box, StringWriter writer, int indent, boolean mustIndent) {
		Vertical v = new Vertical();
		v.options = box.options;
		v.kids = box.kids;
		verticalToText(v, writer, indent + getIs(box.options), true);
	}

	private void alignToText(Align align, StringWriter writer, int indent, boolean mustIndent) {
		String spec = getCells(align.options);
		char[] cs = spec.toCharArray();
		int colWidths[] = new int[cs.length];
		List<Box> each = each(align);
		for (int i = 0; i < cs.length; i++) {
			colWidths[i] = columnWidth(i, each);
		}
		int len = each.size();
		for (int i = 0; i < len; i++) {
			rowToText((Row)each.get(i), writer, indent, colWidths, cs);
			if (i < len - 1) {
				// TODO: vertical rowspacing instead of 1
				writer.append(vfill(1));
			}
		}
	}

	private void rowToText(Row row, StringWriter writer, int indent, int[] colWidths, char[] spec) {
		int height = height(row);
		String output[] = new String[height];
		String fill = hfill(indent);
		// Pre fill the left margin for each line
		Arrays.fill(output, fill);
		
		List<Box> cells = row.kids;
		for (int col = 0; col < cells.size(); col++) {
			Box cell = cells.get(col);
			StringWriter s = new StringWriter();
			toText(cell, s, 0, false);
			String lines[] = s.toString().split("\n");
			for (int i = 0; i < height; i++) {
				output[i] += fillCol(lines[i], colWidths[col], spec[col]);
			}
		}
		for (String line: output) {
			writer.append(line);
		}
	}

	private void textToText(Text box, StringWriter writer, int indent, boolean mustIndent) {
		// TODO: check for mustindent for numbox etc. too
		if (mustIndent) {
			indent(writer, indent);
		}
		writer.append(box.value);
	}

	private List<Box> each(Box box) {
		if (box instanceof Horizontal) {
			return each(((Horizontal)box).kids);
		}
		if (box instanceof Vertical) {
			return each(((Vertical)box).kids);
		}
		if (box instanceof Align) {
			return each(((Align)box).kids);
		}
		if (box instanceof Row) {
			return each(((Row)box).kids);
		}
		if (box instanceof SepList) {
			return eachSepList((SepList)box);
		}
		if (box instanceof Group) {
			return eachGroup((Group)box);
		}
		throw new RuntimeException("unsupported box expression: " + box.getClass());
	}
	
	private List<Box> eachGroup(Group box) {
		int step = getGs(box.options);
		List<Box> result = new ArrayList<Box>();
		
		List<Box> group = new ArrayList<Box>();
		int i = 0;
		for (Box kid: each(box.kids)) {
			group.add(kid);
			if (i % step == 0) {
				wrap(result, box.options, (Object[]) group.toArray(new Box[group.size()]));
				group.clear();
			}
			i++;
		}
		
		return result;
	}
	
	private List<Box> eachSepList(SepList sl) {
		Box prev = null;
		List<Box> result = new ArrayList<Box>();
		for (Box box: each(sl.kids)) {
			if (prev != null) {
				wrap(result, sl.options, prev, getSep(sl.options));
			}
			prev = box;
		}
		if (prev != null) {
			result.add(prev);
		}
		return result;
	}

	private List<Box> each(List<Box> kids) {
		// We just create lists; could make lazy iterators though...
		List<Box> result = new ArrayList<Box>();
		for (Box k: kids) {
			if (k instanceof Group || k instanceof SepList) {
				// Splicing
				for (Box b: each(k)) {
					result.add(b);
				}
			}
			else {
				result.add(k);
			}
		}
		return result;
	}

	private void wrap(List<Box> result, List<Option> options, Object ...args) {
		// TODO: change op options to id; this is wrong now.
		Box op = getOp(options);
		if (op != null) {
			if (op instanceof Vertical) {
				Vertical v1 = (Vertical)op;
				Vertical v2 = new Vertical();
				v2.options = v1.options;
				v2.kids = new ArrayList<Box>();
				wrapArgs(v2.kids, args);
				result.add(v2);
			}
			else if (op instanceof Horizontal) {
				Horizontal v1 = (Horizontal)op;
				Horizontal v2 = new Horizontal();
				v2.options = v1.options;
				v2.kids = new ArrayList<Box>();
				wrapArgs(v2.kids, args);
				result.add(v2);
			}
			else if (op instanceof Indented) {
				Indented v1 = (Indented)op;
				Indented v2 = new Indented();
				v2.options = v1.options;
				v2.kids = new ArrayList<Box>();
				wrapArgs(v2.kids, args);
				result.add(v2);
			}
			else if (op instanceof Row) {
				Row v1 = (Row)op;
				Row v2 = new Row();
				v2.options = v1.options;
				v2.kids = new ArrayList<Box>();
				wrapArgs(v2.kids, args);
				result.add(v2);
			}
			else {
				throw new RuntimeException("unsupported operator: " + op.getClass());
			}
		}
		else {
			wrapArgs(result, args);
		}
	}

	private void wrapArgs(List<Box> result, Object... args) {
		for (Object x: args) {
			if (x instanceof Box) {
				result.add((Box)x);
			}
			else if (x instanceof String) {
				Text txt = new Text();
				txt.value = (String)x;
				result.add(txt);
			}
			else {
				throw new RuntimeException("cannot wrap: " + x);
			}
		}
	}

	private String hfill(int hs) {
		return fill(' ', hs);
	}
	
	private void indent(StringWriter writer, int indent) {
		writer.write(hfill(indent));
	}

	private String fill(char c, int len) {
		char[] cs = new char[len];
		Arrays.fill(cs, c);
		return new String(cs);
	}
	
	private String vfill(int vs) {
		return fill('\n', vs);
	}

	private boolean isVerticallyEmpty(Box box) {
		if (box instanceof Vertical) {
			return ((Vertical)box).kids.isEmpty();
		}
		if (box instanceof Indented) {
			return ((Indented)box).kids.isEmpty();
		}
		if (box instanceof Align) {
			return ((Align)box).kids.isEmpty();
		}
		return false;
	}

	private boolean isHorizontallyEmpty(Box box) {
		if (box instanceof Horizontal) {
			return ((Horizontal)box).kids.isEmpty();
		}
		return false;
	}

	private int width(Box box) {
		if (box instanceof Horizontal) {
			return sumWidth(each(box), getHs(((Horizontal)box).options));
		}
		else if (box instanceof Row) {
			return sumWidth(each(box));
		}
		else if (box instanceof Vertical) {
			return maxWidth(each(box));
		}
		else if (box instanceof Indented) {
			return maxWidth(each(box));
		}
		else if (box instanceof Text) {
			return ((Text)box).value.length();
		}
		else if (box instanceof Align) {
			return maxWidth(each(box));
		}
		throw new RuntimeException("unsupported box expression: " + box.getClass());
	}

	private int height(Box box) {
		if (box instanceof Horizontal) {
			return maxHeight(each(box));
		}
		else if (box instanceof Row) {
			return maxHeight(each(box));
		}
		else if (box instanceof Vertical) {
			return sumHeight(each(box), getVs(((Vertical)box).options));
		}
		else if (box instanceof Indented) {
			return sumHeight(each(box), getVs(((Indented)box).options));
		}
		else if (box instanceof Text) {
			return 1;
		}
		else if (box instanceof Align) {
			return sumHeight(each(box));
		}
		throw new RuntimeException("unsupported box expression: " + box.getClass());
	}

	private int columnWidth(int i, List<Box> boxes) {
		int w = 0;
		for (Box row: boxes) {
			int cur = width(cellAt(i, (Row) row));
			if (cur > w) {
				w = cur;
			}
		}
		return w;
	}

	private Box cellAt(int i, Row row) {
		return row.kids.get(i);
	}

	private int sumHeight(List<Box> boxes) {
		return sumHeight(boxes, 0);
	}

	private int sumHeight(List<Box> boxes, int vs) {
		int h = 0;
		for (Box k: boxes) {
			h += height(k) + vs;
		}
		if (h > 0) {
			return h - vs;
		}
		return h;
	}

	private int maxHeight(List<Box> boxes) {
		int h = 0;
		for (Box k: boxes) {
			int n = height(k);
			if (n > h) {
				h = n;
			}
		}
		return h;
	}

	private int sumWidth(List<Box> boxes, int hs) {
		int w = 0;
		for (Box k: boxes) {
			w += width(k) + hs;
		}
		if (w > 0) {
			return w - hs;
		}
		return w;
	}

	private int sumWidth(List<Box> boxes) {
		return sumWidth(boxes, 0);
	}

	private int maxWidth(List<Box> boxes) {
		int w = 0;
		for (Box k: boxes) {
			int n = width(k);
			if (n > w) {
				w = n;
			}
		}
		return w;
	}

	private int getGs(List<Option> options) {
		Option o = getOption(options, "gs");
		if (o != null) {
			return ((Gs)o).value;
		}
		return 1;
	}

	private int getHs(List<Option> options) {
		Option o = getOption(options, "hs");
		if (o != null) {
			return ((Hs)o).value;
		}
		return 1;
	}

	private int getVs(List<Option> options) {
		Option o = getOption(options, "vs");
		if (o != null) {
			return ((Vs)o).value;
		}
		return 1;
	}
	
	private int getIs(List<Option> options) {
		Option o = getOption(options, "is");
		if (o != null) {
			return ((Is)o).value;
		}
		return 2;
	}
	
	private String getSep(List<Option> options) {
		Option o = getOption(options, "sep");
		if (o != null) {
			return ((Sep)o).value;
		}
		return " ";
	}

	private String getCells(List<Option> options) {
		Option o = getOption(options, "cells");
		if (o != null) {
			return ((Cells)o).spec;
		}
		return null;
	}
	
	private Box getOp(List<Option> options) {
		Option o = getOption(options, "op");
		if (o != null) {
			String box = ((Op)o).box;
			if (box.equals("H")) {
				Horizontal h = new Horizontal();
				h.options = new ArrayList<Option>();
				h.kids = new ArrayList<Box>();
				return h;
			}
			if (box.equals("V")) {
				Vertical v = new Vertical();
				v.options = new ArrayList<Option>();
				v.kids = new ArrayList<Box>();
				return v;
			}
			if (box.equals("R")) {
				Row r = new Row();
				r.options = new ArrayList<Option>();
				r.kids = new ArrayList<Box>();
				return r;
			}
			throw new RuntimeException("unsupported box operator: " + box);
		}
		Horizontal h = new Horizontal();
		h.options = new ArrayList<Option>();
		h.kids = new ArrayList<Box>();
		Hs hs0 = new Hs();
		hs0.value = 0;
		h.options.add(hs0);
		return h;
	}

	private Option getOption(List<Option> options, String name) {
		for (Option o: options) {
			if (name.equals(o.getClass().getSimpleName().toLowerCase())) {
				return o;
			}
		}
		return null;
	}
	
	

}

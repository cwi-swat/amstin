package amstin.models.grammar.parsing.cps;

import java.util.HashMap;
import java.util.Map;

import amstin.models.parsetree.Tree;

public class Memo implements IParser {

	private IParser parser;

	public Memo(IParser parser) {
//		System.out.println("Memoizing: " + parser.getClass());
		this.parser = parser;
	}
	

	@Override
	public int parse(Map<IParser, Map<Integer, Entry>> table, Cnt cnt, final String src, int pos) {
		if (!table.containsKey(parser)) {
			table.put(parser, new HashMap<Integer, Entry>());
		}
		if (!table.get(parser).containsKey(pos)) {
			table.get(parser).put(pos, new Entry());
		}
		
		final Entry entry = table.get(parser).get(pos);
		
		if (entry.cnts.isEmpty()) {
			entry.cnts.add(cnt);
			return parser.parse(table, new MemoCnt(entry), src, pos);
		}
		
		entry.cnts.add(cnt);
		int l = entry.results.size();
		int x = pos;
		for (int i = 0; i < l; i++) {
			// if an item is added to entry.results cflowbelow cnt.apply
			// we do not reprocess it here. I don't know if this is wrong.
			// but we cannot use an iterator over results, because
			// you get concurrentmodification errors (e.g. when parsing
			// multiply nested left recursive expressions).
			int r = entry.results.get(i);
			int y = cnt.apply(r, entry.objects.get(i));
			if (y > x) {
				x = y;
			}
		}
		return x;
	}



	
	private static class MemoCnt implements Cnt {

		private final Entry entry;

		public MemoCnt(Entry entry) {
			this.entry = entry;
		}
		
		@Override
		public int apply(int result, Tree tree) {
			if (!entry.isSubsumed(result, tree)) {
				entry.results.add(result);
				entry.objects.add(tree);
				int x = -1;
				for (Cnt c: entry.cnts) {
					int y = c.apply(result, tree);
					if (y > x) {
						x = y;
					}
				}
				return x;
			}
			return result;
		}
		
	}
	


	
}

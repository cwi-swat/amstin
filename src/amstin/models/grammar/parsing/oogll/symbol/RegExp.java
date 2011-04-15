package amstin.models.grammar.parsing.oogll.symbol;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import amstin.models.grammar.parsing.oogll.GLL;

public class RegExp extends Base implements Terminal{

	private Pattern pattern;

	public RegExp(String regex) {
		this.pattern = Pattern.compile(regex);
	}
	
	protected void setPattern(Pattern pattern) {
		this.pattern = pattern;
	}
	
	protected Pattern getPattern() {
		return pattern;
	}

	@Override
	public String match(int pos, GLL driver) {
		String src = driver.getSrc();
		Matcher m = pattern.matcher(src.subSequence(pos, src.length()));
		if (m.lookingAt()) {
			return result(m);
		}
		return null;
	}


	protected String result(Matcher m) {
		return m.group();
	}
	
	@Override
	public String toString() {
		return pattern.toString();
	}

	@Override
	public boolean isNullable() {
		return pattern.matcher("").matches();
	}

//	@Override
//	public boolean isInFirst(char c) {
//		return false;
//	}


}

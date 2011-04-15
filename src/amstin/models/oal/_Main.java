package amstin.models.oal;

import java.io.IOException;

import amstin.Config;
import amstin.models.grammar.Grammar;
import amstin.models.grammar.parsing.cps.Parser;
import amstin.models.parsetree.ParseTree;

public class _Main {

	public static final String OAL_MDG = Config.PKG_DIR + "/models/oal/oal.mdg";
	public static final String OAL_META = Config.PKG_DIR + "/models/oal/oal.meta";
	public static final String OAL_PKG = Config.PKG + ".models.oal";
	public static final String ELEVATOR_OAL = Config.PKG_DIR + "/models/oal/elevator.oal";
	

	public static void main(String[] args) throws IOException {
		Grammar oal = Parser.parseGrammar(OAL_MDG);
		
		System.out.println(oal);
		String elev = Parser.readPath(ELEVATOR_OAL);
		Parser oalParser = new Parser(oal);
		ParseTree obj = oalParser.parse(elev);

		System.out.println(obj);
		
	}
	
}

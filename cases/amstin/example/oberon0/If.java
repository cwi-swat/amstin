
package amstin.example.oberon0;

import java.util.List;

public class If
    extends Statement
{

    public Expression condition;
    public List<Statement> statements;
    public List<ElsIf> elsIfs;
    public Else els;

}

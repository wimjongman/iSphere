/* Generated By:JJTree: Do not edit this line. ASTArray.java */

package org.medfoster.sqljep;

/**
 * 	This class is used only for IN (exp,exp,...) predicate. 
 *  All children of node with type ASTArray are items of an array.
 * @see org.medfoster.sqljep.function.In
 */
public class ASTArray extends SimpleNode {
  public ASTArray(int id) {
    super(id);
  }

  public ASTArray(Parser p, int id) {
    super(p, id);
  }

  /** Accept the visitor. **/
  public Object jjtAccept(ParserVisitor visitor, Object data) throws ParseException {
    return visitor.visit(this, data);
  }
}

/* Generated By:JJTree: Do not edit this line. ParserVisitor.java */

package org.medfoster.sqljep;

public interface ParserVisitor
{
  public Object visit(SimpleNode node, Object data) throws ParseException;
  public Object visit(ASTStart node, Object data) throws ParseException;
  public Object visit(ASTFunNode node, Object data) throws ParseException;
  public Object visit(ASTVarNode node, Object data) throws ParseException;
  public Object visit(ASTArray node, Object data) throws ParseException;
  public Object visit(ASTConstant node, Object data) throws ParseException;
}

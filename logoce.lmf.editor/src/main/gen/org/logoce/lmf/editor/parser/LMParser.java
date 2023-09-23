// This is a generated file. Not intended for manual editing.
package org.logoce.lmf.editor.parser;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import static org.logoce.lmf.editor.LMTypes.*;
import static com.intellij.lang.parser.GeneratedParserUtilBase.*;
import com.intellij.psi.tree.IElementType;
import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.TokenSet;
import com.intellij.lang.PsiParser;
import com.intellij.lang.LightPsiParser;

@SuppressWarnings({"SimplifiableIfStatement", "UnusedAssignment"})
public class LMParser implements PsiParser, LightPsiParser {

  public ASTNode parse(IElementType t, PsiBuilder b) {
    parseLight(t, b);
    return b.getTreeBuilt();
  }

  public void parseLight(IElementType t, PsiBuilder b) {
    boolean r;
    b = adapt_builder_(t, b, this, null);
    Marker m = enter_section_(b, 0, _COLLAPSE_, null);
    r = parse_root_(t, b);
    exit_section_(b, 0, m, t, r, true, TRUE_CONDITION);
  }

  protected boolean parse_root_(IElementType t, PsiBuilder b) {
    return parse_root_(t, b, 0);
  }

  static boolean parse_root_(IElementType t, PsiBuilder b, int l) {
    return lmFile(b, l + 1);
  }

  /* ********************************************************** */
  // IDENTIFIER
  public static boolean atom(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "atom")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, IDENTIFIER);
    exit_section_(b, m, ATOM, r);
    return r;
  }

  /* ********************************************************** */
  // '(' word* ')'
  public static boolean list(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "list")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, LIST, "<list>");
    r = consumeToken(b, "(");
    r = r && list_1(b, l + 1);
    r = r && consumeToken(b, ")");
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // word*
  private static boolean list_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "list_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!word(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "list_1", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // list*
  static boolean lmFile(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "lmFile")) return false;
    while (true) {
      int c = current_position_(b);
      if (!list(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "lmFile", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // atom | list
  public static boolean word(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "word")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, WORD, "<word>");
    r = atom(b, l + 1);
    if (!r) r = list(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

}

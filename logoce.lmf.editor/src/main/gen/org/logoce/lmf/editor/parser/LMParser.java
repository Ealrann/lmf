// This is a generated file. Not intended for manual editing.
package org.logoce.lmf.editor.parser;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import static org.logoce.lmf.editor.psi.LMTokenTypes.*;
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
  // OPEN_NODE TYPE (word | WHITE_SPACE)* CLOSE_NODE
  public static boolean element(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "element")) return false;
    if (!nextTokenIs(b, OPEN_NODE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, OPEN_NODE, TYPE);
    r = r && element_2(b, l + 1);
    r = r && consumeToken(b, CLOSE_NODE);
    exit_section_(b, m, ELEMENT, r);
    return r;
  }

  // (word | WHITE_SPACE)*
  private static boolean element_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "element_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!element_2_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "element_2", c)) break;
    }
    return true;
  }

  // word | WHITE_SPACE
  private static boolean element_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "element_2_0")) return false;
    boolean r;
    r = word(b, l + 1);
    if (!r) r = consumeToken(b, WHITE_SPACE);
    return r;
  }

  /* ********************************************************** */
  // VALUE LIST_SEPARATOR VALUE (LIST_SEPARATOR VALUE)*
  public static boolean list(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "list")) return false;
    if (!nextTokenIs(b, VALUE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, VALUE, LIST_SEPARATOR, VALUE);
    r = r && list_3(b, l + 1);
    exit_section_(b, m, LIST, r);
    return r;
  }

  // (LIST_SEPARATOR VALUE)*
  private static boolean list_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "list_3")) return false;
    while (true) {
      int c = current_position_(b);
      if (!list_3_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "list_3", c)) break;
    }
    return true;
  }

  // LIST_SEPARATOR VALUE
  private static boolean list_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "list_3_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, LIST_SEPARATOR, VALUE);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // element*
  static boolean lmFile(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "lmFile")) return false;
    while (true) {
      int c = current_position_(b);
      if (!element(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "lmFile", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // NAME (list | VALUE)
  public static boolean named(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "named")) return false;
    if (!nextTokenIs(b, NAME)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, NAME);
    r = r && named_1(b, l + 1);
    exit_section_(b, m, NAMED, r);
    return r;
  }

  // list | VALUE
  private static boolean named_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "named_1")) return false;
    boolean r;
    r = list(b, l + 1);
    if (!r) r = consumeToken(b, VALUE);
    return r;
  }

  /* ********************************************************** */
  // named | list | VALUE | element
  public static boolean word(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "word")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, WORD, "<word>");
    r = named(b, l + 1);
    if (!r) r = list(b, l + 1);
    if (!r) r = consumeToken(b, VALUE);
    if (!r) r = element(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

}

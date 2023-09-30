// This is a generated file. Not intended for manual editing.
package org.logoce.lmf.editor.parser;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import static org.logoce.lmf.editor.psi.LMIntellijTokenTypes.*;
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
  // OPEN_NODE (TYPE_NAME ASSIGN)? TYPE (node | WHITE_SPACE)* CLOSE_NODE WHITE_SPACE?
  public static boolean group(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "group")) return false;
    if (!nextTokenIs(b, OPEN_NODE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, OPEN_NODE);
    r = r && group_1(b, l + 1);
    r = r && consumeToken(b, TYPE);
    r = r && group_3(b, l + 1);
    r = r && consumeToken(b, CLOSE_NODE);
    r = r && group_5(b, l + 1);
    exit_section_(b, m, GROUP, r);
    return r;
  }

  // (TYPE_NAME ASSIGN)?
  private static boolean group_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "group_1")) return false;
    group_1_0(b, l + 1);
    return true;
  }

  // TYPE_NAME ASSIGN
  private static boolean group_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "group_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, TYPE_NAME, ASSIGN);
    exit_section_(b, m, null, r);
    return r;
  }

  // (node | WHITE_SPACE)*
  private static boolean group_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "group_3")) return false;
    while (true) {
      int c = current_position_(b);
      if (!group_3_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "group_3", c)) break;
    }
    return true;
  }

  // node | WHITE_SPACE
  private static boolean group_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "group_3_0")) return false;
    boolean r;
    r = node(b, l + 1);
    if (!r) r = consumeToken(b, WHITE_SPACE);
    return r;
  }

  // WHITE_SPACE?
  private static boolean group_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "group_5")) return false;
    consumeToken(b, WHITE_SPACE);
    return true;
  }

  /* ********************************************************** */
  // (VALUE_NAME ASSIGN)? val (LIST_SEPARATOR val)*
  public static boolean leaf(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "leaf")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, LEAF, "<leaf>");
    r = leaf_0(b, l + 1);
    r = r && val(b, l + 1);
    r = r && leaf_2(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (VALUE_NAME ASSIGN)?
  private static boolean leaf_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "leaf_0")) return false;
    leaf_0_0(b, l + 1);
    return true;
  }

  // VALUE_NAME ASSIGN
  private static boolean leaf_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "leaf_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, VALUE_NAME, ASSIGN);
    exit_section_(b, m, null, r);
    return r;
  }

  // (LIST_SEPARATOR val)*
  private static boolean leaf_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "leaf_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!leaf_2_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "leaf_2", c)) break;
    }
    return true;
  }

  // LIST_SEPARATOR val
  private static boolean leaf_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "leaf_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LIST_SEPARATOR);
    r = r && val(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // group*
  static boolean lmFile(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "lmFile")) return false;
    while (true) {
      int c = current_position_(b);
      if (!group(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "lmFile", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // leaf | group
  public static boolean node(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "node")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, NODE, "<node>");
    r = leaf(b, l + 1);
    if (!r) r = group(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // QUOTE? VALUE QUOTE?
  public static boolean val(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "val")) return false;
    if (!nextTokenIs(b, "<val>", QUOTE, VALUE)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, VAL, "<val>");
    r = val_0(b, l + 1);
    r = r && consumeToken(b, VALUE);
    r = r && val_2(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // QUOTE?
  private static boolean val_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "val_0")) return false;
    consumeToken(b, QUOTE);
    return true;
  }

  // QUOTE?
  private static boolean val_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "val_2")) return false;
    consumeToken(b, QUOTE);
    return true;
  }

}

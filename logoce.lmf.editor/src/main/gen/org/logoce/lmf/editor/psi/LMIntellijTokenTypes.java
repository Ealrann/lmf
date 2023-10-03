// This is a generated file. Not intended for manual editing.
package org.logoce.lmf.editor.psi;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.PsiElement;
import com.intellij.lang.ASTNode;
import org.logoce.lmf.editor.psi.impl.*;

public interface LMIntellijTokenTypes {

  IElementType GROUP = new LMElementType("GROUP");
  IElementType GROUP_TYPE = new LMElementType("GROUP_TYPE");
  IElementType LEAF = new LMElementType("LEAF");
  IElementType NODE = new LMElementType("NODE");
  IElementType VAL = new LMElementType("VAL");

  IElementType ASSIGN = new LMTokenType("ASSIGN");
  IElementType BAD_CHARACTER = new LMTokenType("BAD_CHARACTER");
  IElementType CLOSE_NODE = new LMTokenType("CLOSE_NODE");
  IElementType LIST_SEPARATOR = new LMTokenType("LIST_SEPARATOR");
  IElementType OPEN_NODE = new LMTokenType("OPEN_NODE");
  IElementType QUOTE = new LMTokenType("QUOTE");
  IElementType TYPE = new LMTokenType("TYPE");
  IElementType TYPE_NAME = new LMTokenType("TYPE_NAME");
  IElementType VALUE = new LMTokenType("VALUE");
  IElementType VALUE_NAME = new LMTokenType("VALUE_NAME");
  IElementType WHITE_SPACE = new LMTokenType("WHITE_SPACE");

  class Factory {
    public static PsiElement createElement(ASTNode node) {
      IElementType type = node.getElementType();
      if (type == GROUP) {
        return new LMFGroupImpl(node);
      }
      else if (type == GROUP_TYPE) {
        return new LMFGroupTypeImpl(node);
      }
      else if (type == LEAF) {
        return new LMFLeafImpl(node);
      }
      else if (type == NODE) {
        return new LMFNodeImpl(node);
      }
      else if (type == VAL) {
        return new LMFValImpl(node);
      }
      throw new AssertionError("Unknown element type: " + type);
    }
  }
}

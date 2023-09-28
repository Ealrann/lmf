// This is a generated file. Not intended for manual editing.
package org.logoce.lmf.editor.psi;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.PsiElement;
import com.intellij.lang.ASTNode;
import org.logoce.lmf.editor.psi.impl.*;

public interface LMTokenTypes {

  IElementType ELEMENT = new LMElementType("ELEMENT");
  IElementType LIST = new LMElementType("LIST");
  IElementType NAMED = new LMElementType("NAMED");
  IElementType VAL = new LMElementType("VAL");
  IElementType WORD = new LMElementType("WORD");

  IElementType ASSIGN = new LMTokenType("ASSIGN");
  IElementType CLOSE_NODE = new LMTokenType("CLOSE_NODE");
  IElementType LIST_SEPARATOR = new LMTokenType("LIST_SEPARATOR");
  IElementType NAME = new LMTokenType("NAME");
  IElementType OPEN_NODE = new LMTokenType("OPEN_NODE");
  IElementType QUOTE = new LMTokenType("QUOTE");
  IElementType TYPE = new LMTokenType("TYPE");
  IElementType VALUE = new LMTokenType("VALUE");
  IElementType WHITE_SPACE = new LMTokenType("WHITE_SPACE");

  class Factory {
    public static PsiElement createElement(ASTNode node) {
      IElementType type = node.getElementType();
      if (type == ELEMENT) {
        return new LMFElementImpl(node);
      }
      else if (type == LIST) {
        return new LMFListImpl(node);
      }
      else if (type == NAMED) {
        return new LMFNamedImpl(node);
      }
      else if (type == VAL) {
        return new LMFValImpl(node);
      }
      else if (type == WORD) {
        return new LMFWordImpl(node);
      }
      throw new AssertionError("Unknown element type: " + type);
    }
  }
}

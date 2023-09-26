// This is a generated file. Not intended for manual editing.
package org.logoce.lmf.editor.psi;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.PsiElement;
import com.intellij.lang.ASTNode;
import org.logoce.lmf.editor.psi.impl.*;

public interface LMTokenTypes {

  IElementType LIST = new LMElementType("LIST");
  IElementType TYPE = new LMElementType("TYPE");
  IElementType VALUE = new LMElementType("VALUE");
  IElementType WORD = new LMElementType("WORD");

  IElementType CLOSE_NODE = new LMTokenType(")");
  IElementType IDENTIFIER = new LMTokenType("IDENTIFIER");
  IElementType OPEN_NODE = new LMTokenType("(");

  class Factory {
    public static PsiElement createElement(ASTNode node) {
      IElementType type = node.getElementType();
      if (type == LIST) {
        return new LMFListImpl(node);
      }
      else if (type == TYPE) {
        return new LMFTypeImpl(node);
      }
      else if (type == VALUE) {
        return new LMFValueImpl(node);
      }
      else if (type == WORD) {
        return new LMFWordImpl(node);
      }
      throw new AssertionError("Unknown element type: " + type);
    }
  }
}

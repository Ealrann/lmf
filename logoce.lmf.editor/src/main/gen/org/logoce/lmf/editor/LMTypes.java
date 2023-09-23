// This is a generated file. Not intended for manual editing.
package org.logoce.lmf.editor;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.PsiElement;
import com.intellij.lang.ASTNode;
import org.logoce.lmf.editor.psi.LMElementType;
import org.logoce.lmf.editor.psi.LMTokenType;
import org.logoce.lmf.editor.psi.impl.*;

public interface LMTypes {

  IElementType ATOM = new LMElementType("ATOM");
  IElementType LIST = new LMElementType("LIST");
  IElementType WORD = new LMElementType("WORD");

  IElementType IDENTIFIER = new LMTokenType("IDENTIFIER");

  class Factory {
    public static PsiElement createElement(ASTNode node) {
      IElementType type = node.getElementType();
      if (type == ATOM) {
        return new LMFAtomImpl(node);
      }
      else if (type == LIST) {
        return new LMFListImpl(node);
      }
      else if (type == WORD) {
        return new LMFWordImpl(node);
      }
      throw new AssertionError("Unknown element type: " + type);
    }
  }
}

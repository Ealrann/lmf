// This is a generated file. Not intended for manual editing.
package org.logoce.lmf.editor.psi;

import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiElement;
import org.logoce.lmf.editor.ref.LMNamedElement;

public class LMFVisitor extends PsiElementVisitor {

  public void visitGroup(@NotNull LMFGroup o) {
    visitLMNamedElement(o);
  }

  public void visitGroupType(@NotNull LMFGroupType o) {
    visitPsiElement(o);
  }

  public void visitLeaf(@NotNull LMFLeaf o) {
    visitPsiElement(o);
  }

  public void visitVal(@NotNull LMFVal o) {
    visitPsiElement(o);
  }

  public void visitLMNamedElement(@NotNull LMNamedElement o) {
    visitElement(o);
  }

  public void visitPsiElement(@NotNull PsiElement o) {
    visitElement(o);
  }

}

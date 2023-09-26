// This is a generated file. Not intended for manual editing.
package org.logoce.lmf.editor.psi;

import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiElement;

public class LMFVisitor extends PsiElementVisitor {

  public void visitList(@NotNull LMFList o) {
    visitPsiElement(o);
  }

  public void visitType(@NotNull LMFType o) {
    visitPsiElement(o);
  }

  public void visitValue(@NotNull LMFValue o) {
    visitPsiElement(o);
  }

  public void visitWord(@NotNull LMFWord o) {
    visitPsiElement(o);
  }

  public void visitPsiElement(@NotNull PsiElement o) {
    visitElement(o);
  }

}

// This is a generated file. Not intended for manual editing.
package org.logoce.lmf.editor.psi;

import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiElement;

public class LMFVisitor extends PsiElementVisitor {

  public void visitElement(@NotNull LMFElement o) {
    visitPsiElement(o);
  }

  public void visitList(@NotNull LMFList o) {
    visitPsiElement(o);
  }

  public void visitListExtension(@NotNull LMFListExtension o) {
    visitPsiElement(o);
  }

  public void visitNamed(@NotNull LMFNamed o) {
    visitPsiElement(o);
  }

  public void visitVal(@NotNull LMFVal o) {
    visitPsiElement(o);
  }

  public void visitWord(@NotNull LMFWord o) {
    visitPsiElement(o);
  }

  public void visitPsiElement(@NotNull PsiElement o) {
    visitElement(o);
  }

}

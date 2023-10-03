// This is a generated file. Not intended for manual editing.
package org.logoce.lmf.editor.psi;

import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiElement;

public class LMFVisitor extends PsiElementVisitor {

  public void visitGroup(@NotNull LMFGroup o) {
    visitPsiElement(o);
  }

  public void visitGroupType(@NotNull LMFGroupType o) {
    visitPsiElement(o);
  }

  public void visitLeaf(@NotNull LMFLeaf o) {
    visitPsiElement(o);
  }

  public void visitNode(@NotNull LMFNode o) {
    visitPsiElement(o);
  }

  public void visitVal(@NotNull LMFVal o) {
    visitPsiElement(o);
  }

  public void visitPsiElement(@NotNull PsiElement o) {
    visitElement(o);
  }

}

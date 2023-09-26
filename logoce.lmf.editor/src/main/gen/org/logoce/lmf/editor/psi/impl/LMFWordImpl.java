// This is a generated file. Not intended for manual editing.
package org.logoce.lmf.editor.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static org.logoce.lmf.editor.psi.LMTokenTypes.*;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import org.logoce.lmf.editor.psi.*;

public class LMFWordImpl extends ASTWrapperPsiElement implements LMFWord {

  public LMFWordImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull LMFVisitor visitor) {
    visitor.visitWord(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof LMFVisitor) accept((LMFVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public LMFElement getElement() {
    return findChildByClass(LMFElement.class);
  }

  @Override
  @Nullable
  public LMFList getList() {
    return findChildByClass(LMFList.class);
  }

  @Override
  @Nullable
  public LMFNamed getNamed() {
    return findChildByClass(LMFNamed.class);
  }

}

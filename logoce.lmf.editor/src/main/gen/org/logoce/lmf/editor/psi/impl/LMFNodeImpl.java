// This is a generated file. Not intended for manual editing.
package org.logoce.lmf.editor.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static org.logoce.lmf.editor.psi.LMIntellijTokenTypes.*;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import org.logoce.lmf.editor.psi.*;

public class LMFNodeImpl extends ASTWrapperPsiElement implements LMFNode {

  public LMFNodeImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull LMFVisitor visitor) {
    visitor.visitNode(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof LMFVisitor) accept((LMFVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public LMFGroup getGroup() {
    return findChildByClass(LMFGroup.class);
  }

  @Override
  @Nullable
  public LMFLeaf getLeaf() {
    return findChildByClass(LMFLeaf.class);
  }

}

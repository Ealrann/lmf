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

public class LMFGroupImpl extends ASTWrapperPsiElement implements LMFGroup {

  public LMFGroupImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull LMFVisitor visitor) {
    visitor.visitGroup(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof LMFVisitor) accept((LMFVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<LMFGroup> getGroupList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, LMFGroup.class);
  }

  @Override
  @NotNull
  public LMFGroupType getGroupType() {
    return findNotNullChildByClass(LMFGroupType.class);
  }

  @Override
  @NotNull
  public List<LMFLeaf> getLeafList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, LMFLeaf.class);
  }

}

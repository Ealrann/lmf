// This is a generated file. Not intended for manual editing.
package org.logoce.lmf.editor.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import org.logoce.lmf.editor.ref.LMNamedElement;
import com.intellij.util.IncorrectOperationException;

public interface LMFGroup extends LMNamedElement {

  @NotNull
  List<LMFGroup> getGroupList();

  @NotNull
  LMFGroupType getGroupType();

  @NotNull
  List<LMFLeaf> getLeafList();

  //WARNING: getKey(...) is skipped
  //matching getKey(LMFGroup, ...)
  //methods are not found in LMPsiImplUtil

  //WARNING: getValue(...) is skipped
  //matching getValue(LMFGroup, ...)
  //methods are not found in LMPsiImplUtil

  //WARNING: getName(...) is skipped
  //matching getName(LMFGroup, ...)
  //methods are not found in LMPsiImplUtil

  PsiElement setName(@NotNull String name) throws IncorrectOperationException;

  @Nullable PsiElement getNameIdentifier();

}

// This is a generated file. Not intended for manual editing.
package org.logoce.lmf.editor.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface LMFGroup extends PsiElement {

  @NotNull
  List<LMFGroup> getGroupList();

  @NotNull
  LMFGroupType getGroupType();

  @NotNull
  List<LMFLeaf> getLeafList();

}

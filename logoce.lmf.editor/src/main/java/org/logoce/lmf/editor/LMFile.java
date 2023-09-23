package org.logoce.lmf.editor;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import org.jetbrains.annotations.NotNull;

public class LMFile extends PsiFileBase
{

  public LMFile(@NotNull FileViewProvider viewProvider) {
    super(viewProvider, LMLanguage.INSTANCE);
  }

  @NotNull
  @Override
  public FileType getFileType() {
    return LMFileType.INSTANCE;
  }

  @Override
  public String toString() {
    return "Simple File";
  }

}
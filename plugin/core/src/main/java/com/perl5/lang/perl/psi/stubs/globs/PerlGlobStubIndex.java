/*
 * Copyright 2015-2022 Alexandr Evstigneev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.perl5.lang.perl.psi.stubs.globs;

import com.intellij.psi.stubs.StubIndexExtension;
import com.intellij.psi.stubs.StubIndexKey;
import com.perl5.lang.perl.psi.PsiPerlGlobVariable;
import com.perl5.lang.perl.psi.stubs.PerlStubIndexBase;
import org.jetbrains.annotations.NotNull;


public class PerlGlobStubIndex extends PerlStubIndexBase<PsiPerlGlobVariable> {
  public static final int VERSION = 5;
  public static final StubIndexKey<String, PsiPerlGlobVariable> KEY_GLOB = StubIndexKey.createIndexKey("perl.glob");

  @Override
  public int getVersion() {
    return super.getVersion() + VERSION;
  }

  @Override
  public @NotNull StubIndexKey<String, PsiPerlGlobVariable> getKey() {
    return KEY_GLOB;
  }

  @Override
  protected @NotNull Class<PsiPerlGlobVariable> getPsiClass() {
    return PsiPerlGlobVariable.class;
  }

  public static @NotNull PerlGlobStubIndex getInstance() {
    return StubIndexExtension.EP_NAME.findExtensionOrFail(PerlGlobStubIndex.class);
  }
}

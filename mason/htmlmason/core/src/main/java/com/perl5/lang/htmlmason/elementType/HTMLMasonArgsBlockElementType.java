/*
 * Copyright 2015-2024 Alexandr Evstigneev
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

package com.perl5.lang.htmlmason.elementType;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.*;
import com.perl5.lang.htmlmason.HTMLMasonLanguage;
import com.perl5.lang.htmlmason.parser.psi.HTMLMasonArgsBlock;
import com.perl5.lang.htmlmason.parser.psi.impl.HTMLMasonArgsBlockImpl;
import com.perl5.lang.htmlmason.parser.stubs.HTMLMasonArgsBlockStub;
import com.perl5.lang.htmlmason.parser.stubs.impl.HTMLMasonArgsBlockStubImpl;
import com.perl5.lang.perl.parser.elementTypes.PsiElementProvider;
import com.perl5.lang.perl.psi.utils.PerlSubArgument;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;


public class HTMLMasonArgsBlockElementType extends IStubElementType<HTMLMasonArgsBlockStub, HTMLMasonArgsBlock>
  implements PsiElementProvider {
  public HTMLMasonArgsBlockElementType(@NotNull @NonNls String debugName) {
    super(debugName, HTMLMasonLanguage.INSTANCE);
  }

  @Override
  public @NotNull PsiElement getPsiElement(@NotNull ASTNode node) {
    return new HTMLMasonArgsBlockImpl(node);
  }


  @Override
  public HTMLMasonArgsBlock createPsi(@NotNull HTMLMasonArgsBlockStub stub) {
    return new HTMLMasonArgsBlockImpl(stub, this);
  }

  @Override
  public @NotNull HTMLMasonArgsBlockStub createStub(@NotNull HTMLMasonArgsBlock psi, StubElement parentStub) {
    return new HTMLMasonArgsBlockStubImpl(parentStub, this, psi.getArgumentsList());
  }

  @Override
  public @NotNull String getExternalId() {
    return "HTML::Mason::" + super.toString();
  }

  @Override
  public void serialize(@NotNull HTMLMasonArgsBlockStub stub, @NotNull StubOutputStream dataStream) throws IOException {
    PerlSubArgument.serializeList(dataStream, stub.getArgumentsList());
  }

  @Override
  public @NotNull HTMLMasonArgsBlockStub deserialize(@NotNull StubInputStream dataStream, StubElement parentStub) throws IOException {
    return new HTMLMasonArgsBlockStubImpl(parentStub, this, PerlSubArgument.deserializeList(dataStream));
  }

  /**
   * @implNote we don't need to index this stub, only store
   */
  @Override
  public void indexStub(@NotNull HTMLMasonArgsBlockStub stub, @NotNull IndexSink sink) {
  }
}

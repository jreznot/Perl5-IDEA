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

package com.intellij.openapi.projectRoots.impl;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkTypeId;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.messages.Topic;
import com.perl5.lang.perl.idea.PerlPathMacros;
import com.perl5.lang.perl.idea.sdk.PerlSdkAdditionalData;
import com.perl5.lang.perl.idea.sdk.PerlSdkType;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.List;

@State(
  name = "PerlInterpreters",
  storages = @Storage(PerlPathMacros.PERL5_APP_SETTINGS_FILE)
)
public class PerlSdkTable extends ProjectJdkTable implements PersistentStateComponent<Element> {
  private static final Logger LOG = Logger.getInstance(PerlSdkTable.class);
  public static final Topic<Listener> PERL_TABLE_TOPIC = Topic.create("Perl Interpreters table", Listener.class);

  private static final String PERL = "perl";

  private final List<Sdk> myInterpretersList = new ArrayList<>();
  private final MessageBus myMessageBus;

  public PerlSdkTable() {
    myMessageBus = ApplicationManager.getApplication().getMessageBus();
  }

  @Override
  public void loadState(@NotNull Element element) {
    myInterpretersList.clear();

    for (Element child : element.getChildren(PERL)) {
      ProjectJdkImpl sdk = new ProjectJdkImpl("", PerlSdkType.INSTANCE);
      sdk.readExternal(child);
      if( PerlSdkAdditionalData.from((Sdk)sdk) != null){
        myInterpretersList.add(sdk);
      }
      else{
        LOG.warn("Dropped sdk with missing additional data, bad migration in 2024.1 EAP, re-add the sdk: " + sdk);
      }
    }
  }

  @Override
  public Element getState() {
    Element element = new Element("state");
    for (Sdk sdk : myInterpretersList) {
      if( PerlSdkAdditionalData.from(sdk) != null){
        Element e = new Element(PERL);
        ((ProjectJdkImpl)sdk).writeExternal(e);
        element.addContent(e);
      }
      else{
        LOG.warn("Dropped sdk with missing additional data, corrupted on creation 2024.1 EAP, re-add the sdk: " + sdk);
      }
    }
    return element;
  }

  @Override
  public @Nullable Sdk findJdk(@Nullable String name) {
    for (Sdk interpreter : myInterpretersList) {
      if (StringUtil.equals(name, interpreter.getName())) {
        return interpreter;
      }
    }
    return null;
  }

  @Override
  public @Nullable Sdk findJdk(@NotNull String name, @NotNull String type) {
    return findJdk(name);
  }

  /**
   * @deprecated use com.intellij.openapi.projectRoots.impl.PerlSdkTable#getInterpreters() instead
   */
  @Deprecated
  @Override
  public Sdk @NotNull [] getAllJdks() {
    LOG.error("Not expected to be invoked");
    throw new AbstractMethodError();
  }

  public @NotNull List<Sdk> getInterpreters() {
    return new ArrayList<>(myInterpretersList);
  }

  @Override
  public @NotNull List<Sdk> getSdksOfType(@NotNull SdkTypeId type) {
    throw new IncorrectOperationException();
  }

  @Override
  public void addJdk(@NotNull Sdk jdk) {
    if( PerlSdkAdditionalData.from(jdk) == null){
      LOG.error("Attempt to add corrupted sdk: " + jdk);
    }
    else{
      myInterpretersList.add(jdk);
      myMessageBus.syncPublisher(PERL_TABLE_TOPIC).jdkAdded(jdk);
    }
  }

  @Override
  public void removeJdk(@NotNull Sdk jdk) {
    myInterpretersList.remove(jdk);
    myMessageBus.syncPublisher(PERL_TABLE_TOPIC).jdkRemoved(jdk);
    if (jdk instanceof Disposable disposable) {
      Disposer.dispose(disposable);
    }
  }

  @Override
  public void updateJdk(@NotNull Sdk originalJdk, @NotNull Sdk modifiedJdk) {
    final String previousName = originalJdk.getName();
    final String newName = modifiedJdk.getName();

    MethodHandle method;
    try {
      method = MethodHandles
        .privateLookupIn(ProjectJdkImpl.class, MethodHandles.lookup())
        .findVirtual(ProjectJdkImpl.class, "copyTo", MethodType.methodType(void.class, ProjectJdkImpl.class));
    }
    catch (NoSuchMethodException e) {
      throw new RuntimeException("Missing copyTo method", e);
    }
    catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }

    try {
      method.invoke(modifiedJdk, originalJdk);
    }
    catch (Throwable e) {
      throw new RuntimeException(e);
    }

    if (!previousName.equals(newName)) {
      // fire changes because after renaming JDK its name may match the associated jdk name of modules/project
      myMessageBus.syncPublisher(PERL_TABLE_TOPIC).jdkNameChanged(originalJdk, previousName);
    }
  }

  @Override
  public @NotNull SdkTypeId getDefaultSdkType() {
    return PerlSdkType.INSTANCE;
  }

  @Override
  public @NotNull SdkTypeId getSdkTypeByName(@NotNull String name) {
    return PerlSdkType.INSTANCE;
  }

  @Override
  public @NotNull Sdk createSdk(@NotNull String name, @NotNull SdkTypeId sdkType) {
    return ProjectJdkTable.getInstance().createSdk(name, PerlSdkType.INSTANCE);
  }

  public @NotNull ProjectJdkImpl createSdk(@NotNull String name) {
    return (ProjectJdkImpl)createSdk(name, PerlSdkType.INSTANCE);
  }

  public static PerlSdkTable getInstance() {
    return ApplicationManager.getApplication().getService(PerlSdkTable.class);
  }

  @Override
  public void saveOnDisk() {
    throw new RuntimeException("Not supposed to be used for the perl plugin, not implemented");
  }
}

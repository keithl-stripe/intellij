/*
 * Copyright 2021 The Bazel Authors. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.idea.blaze.base.sync.data;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth8.assertThat;
import static org.mockito.Mockito.when;

import com.google.idea.blaze.base.logging.LoggedDirectoryProvider;
import com.google.idea.blaze.base.logging.LoggedDirectoryProvider.LoggedDirectory;
import com.google.idea.blaze.base.settings.BlazeImportSettings;
import com.google.idea.blaze.base.settings.BlazeImportSettingsManager;
import com.google.idea.blaze.base.settings.BuildSystem;
import com.google.idea.blaze.base.sync.data.BlazeDataStorage.LoggedProjectCacheDirectory;
import com.google.idea.testing.IntellijRule;
import java.util.Optional;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

/** Tests for {@link LoggedProjectCacheDirectory}. */
@RunWith(JUnit4.class)
public class LoggedProjectCacheDirectoryTest {

  @Rule public final IntellijRule intellij = new IntellijRule();
  @Rule public final MockitoRule mockito = MockitoJUnit.rule();
  @Rule public final TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Mock private BlazeImportSettingsManager importSettingsManager;

  private final LoggedDirectoryProvider directoryProvider = new LoggedProjectCacheDirectory();

  @Before
  public void setUp() throws Exception {
    intellij.registerProjectService(BlazeImportSettingsManager.class, importSettingsManager);

    BlazeImportSettings settings = createSettings(BuildSystem.Blaze);
    when(importSettingsManager.getImportSettings()).thenReturn(settings);
  }

  @Test
  public void directoryIsProvided() {
    Optional<LoggedDirectory> loggedDirectory =
        directoryProvider.getLoggedDirectory(intellij.getProject());

    assertThat(loggedDirectory).isPresent();
    assertThat(loggedDirectory.get().path()).isNotNull();
  }

  @Test
  public void loggedDirectoryHasCorrectPurpose() {
    Optional<LoggedDirectory> loggedDirectory =
        directoryProvider.getLoggedDirectory(intellij.getProject());

    assertThat(loggedDirectory).isPresent();
    assertThat(loggedDirectory.get().purpose()).isEqualTo("Build-related project cache");
  }

  @Test
  public void loggedDirectoryHasCorrectOriginatingIdePart() {
    BlazeImportSettings settings = createSettings(BuildSystem.Blaze);
    when(importSettingsManager.getImportSettings()).thenReturn(settings);

    Optional<LoggedDirectory> loggedDirectory =
        directoryProvider.getLoggedDirectory(intellij.getProject());

    assertThat(loggedDirectory).isPresent();
    assertThat(loggedDirectory.get().originatingIdePart()).isEqualTo("Blaze plugin");
  }

  @Test
  public void originatingIdePartIsSensitiveToBuildSystem() {
    BlazeImportSettings bazelSettings = createSettings(BuildSystem.Bazel);
    BlazeImportSettings blazeSettings = createSettings(BuildSystem.Blaze);

    when(importSettingsManager.getImportSettings()).thenReturn(bazelSettings);
    Optional<LoggedDirectory> bazelDirectory =
        directoryProvider.getLoggedDirectory(intellij.getProject());

    when(importSettingsManager.getImportSettings()).thenReturn(blazeSettings);
    Optional<LoggedDirectory> blazeDirectory =
        directoryProvider.getLoggedDirectory(intellij.getProject());

    assertThat(bazelDirectory).isPresent();
    assertThat(bazelDirectory.get().originatingIdePart()).contains("Bazel");
    assertThat(blazeDirectory).isPresent();
    assertThat(blazeDirectory.get().originatingIdePart()).contains("Blaze");
  }

  private static BlazeImportSettings createSettings(BuildSystem buildSystem) {
    return new BlazeImportSettings(
        /* workspaceRoot= */ "",
        /* projectName= */ "",
        /* projectDataDirectory= */ "",
        /* projectViewFile= */ "",
        buildSystem);
  }
}

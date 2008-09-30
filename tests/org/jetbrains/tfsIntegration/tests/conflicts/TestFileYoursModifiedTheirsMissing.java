/*
 * Copyright 2000-2008 JetBrains s.r.o.
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

package org.jetbrains.tfsIntegration.tests.conflicts;

import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.VcsException;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.tfsIntegration.core.tfs.conflicts.ContentMerger;
import org.jetbrains.tfsIntegration.core.tfs.conflicts.NameMerger;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class TestFileYoursModifiedTheirsMissing extends TestConflicts {

  private FilePath myBaseFile;

  protected boolean canMerge() {
    return false;
  }

  protected void preparePaths() {
    myBaseFile = getChildPath(mySandboxRoot, BASE_FILENAME);
  }

  protected void prepareBaseRevision() {
    createFileInCommand(myBaseFile, BASE_CONTENT);
  }

  protected void prepareTargetRevision() throws VcsException, IOException {
    deleteFileInCommand(myBaseFile);
  }

  protected void makeLocalChanges() throws IOException, VcsException {
    editFiles(myBaseFile);
    setFileContent(myBaseFile, YOURS_CONTENT);
  }

  protected void checkResolvedYoursState() throws VcsException {
    getChanges().assertTotalItems(1);
    getChanges().assertModified(myBaseFile);

    assertFolder(mySandboxRoot, 1);
    assertFile(myBaseFile, YOURS_CONTENT, true);
  }

  protected void checkResolvedTheirsState() throws VcsException {
    getChanges().assertTotalItems(0);

    assertFolder(mySandboxRoot, 0);
  }

  protected void checkResolvedMergeState() throws VcsException {
    Assert.fail("can't merge");
  }

  @Nullable
  protected NameMerger getNameMerger() {
    return null;
  }

  @Nullable
  protected ContentMerger getContentMerger() {
    return null;
  }

  @Test
  public void testAcceptYours() throws VcsException, IOException {
    super.testAcceptYours();
  }

  @Test
  public void testAcceptTheirs() throws VcsException, IOException {
    super.testAcceptTheirs();
  }

  @Test
  public void testAcceptMerge() throws VcsException, IOException {
    super.testAcceptMerge();
  }
}

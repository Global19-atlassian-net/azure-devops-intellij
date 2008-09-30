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

package org.jetbrains.tfsIntegration.core;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.update.FileGroup;
import com.intellij.openapi.vcs.update.UpdateEnvironment;
import com.intellij.openapi.vcs.update.UpdateSession;
import com.intellij.openapi.vcs.update.UpdatedFiles;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.tfsIntegration.core.tfs.*;
import org.jetbrains.tfsIntegration.core.tfs.conflicts.ConflictsEnvironment;
import org.jetbrains.tfsIntegration.core.tfs.conflicts.ResolveConflictHelper;
import org.jetbrains.tfsIntegration.core.tfs.operations.ApplyGetOperations;
import org.jetbrains.tfsIntegration.core.tfs.version.LatestVersionSpec;
import org.jetbrains.tfsIntegration.core.tfs.version.VersionSpecBase;
import org.jetbrains.tfsIntegration.exceptions.TfsException;
import org.jetbrains.tfsIntegration.stubs.versioncontrol.repository.GetOperation;
import org.jetbrains.tfsIntegration.stubs.versioncontrol.repository.RecursionType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class TFSUpdateEnvironment implements UpdateEnvironment {
  private final Project myProject;

  TFSUpdateEnvironment(final Project project) {
    myProject = project;
  }

  public void fillGroups(final UpdatedFiles updatedFiles) {
  }

  @NotNull
  public UpdateSession updateDirectories(@NotNull final FilePath[] contentRoots,
                                         final UpdatedFiles updatedFiles,
                                         final ProgressIndicator progressIndicator) throws ProcessCanceledException {

    // TODO we don't want to get a dialog for each workspace!
    final List<VcsException> exceptions = new ArrayList<VcsException>();
    try {
      List<FilePath> orphanPaths =
        WorkstationHelper.processByWorkspaces(Arrays.asList(contentRoots), true, new WorkstationHelper.VoidProcessDelegate() {
          public void executeRequest(final WorkspaceInfo workspace, final List<ItemPath> paths) throws TfsException {

            VersionSpecBase version = LatestVersionSpec.INSTANCE;
            RecursionType recursionType = RecursionType.Full;
            TFSProjectConfiguration configuration = TFSProjectConfiguration.getInstance(myProject);
            if (configuration != null) {
              version = configuration.getUpdateWorkspaceInfo(workspace).getVersion();
              recursionType = configuration.UPDATE_RECURSIVELY ? RecursionType.Full : RecursionType.None;
            }
            TFSProgressUtil.setProgressText(progressIndicator, "Request update information");

            // 1. query get operations for contentRoots - to let server know which version we need to report corresponding server conflicts
            List<VersionControlServer.GetRequestParams> requests = new ArrayList<VersionControlServer.GetRequestParams>(paths.size());
            for (ItemPath path : paths) {
              requests.add(new VersionControlServer.GetRequestParams(path.getServerPath(), recursionType, version));
              TFSProgressUtil.checkCanceled(progressIndicator);
            }
            // call get() to let server know revision interested in
            List<GetOperation> operations = workspace.getServer().getVCS().get(workspace.getName(), workspace.getOwnerName(), requests);
            // execute GetOperation-s, conflicting ones will be skipped
            final Collection<VcsException> applyErrors = ApplyGetOperations
              .execute(myProject, workspace, operations, progressIndicator, updatedFiles, ApplyGetOperations.DownloadMode.ALLOW);
            exceptions.addAll(applyErrors);

            // resolve all conflicts
            ResolveConflictHelper resolveConflictHelper = new ResolveConflictHelper(myProject, workspace, paths, updatedFiles);
            resolveConflictHelper.reloadConflicts();
            if (!resolveConflictHelper.getConflicts().isEmpty()) {
              ConflictsEnvironment.getResolveConflictsHandler().resolveConflicts(resolveConflictHelper);
            }

            // TODO content roots can be renamed while executing
            TfsFileUtil.refreshAndInvalidate(myProject, contentRoots, false);
          }
        });
      for (FilePath orphanPath : orphanPaths) {
        updatedFiles.getGroupById(FileGroup.UNKNOWN_ID).add(orphanPath.getPresentableUrl());
      }
    }
    catch (TfsException e) {
      //noinspection ThrowableInstanceNeverThrown
      exceptions.add(new VcsException(e));
    }

    return new UpdateSession() {
      @NotNull
      public List<VcsException> getExceptions() {
        return exceptions;
      }

      public void onRefreshFilesCompleted() {
      }

      public boolean isCanceled() {
        return false;
      }
    };
  }

  @Nullable
  public Configurable createConfigurable(final Collection<FilePath> files) {
    if (files.isEmpty()) {
      return null;
    }
    final Ref<Boolean> mappingFound = new Ref<Boolean>(false);
    try {
      WorkstationHelper.processByWorkspaces(files, true, new WorkstationHelper.VoidProcessDelegate() {
        public void executeRequest(final WorkspaceInfo workspace, final List<ItemPath> paths) throws TfsException {
          mappingFound.set(true);
        }
      });
    }
    catch (TfsException e) {
      // skip for now, errors will be reported by updateDirectories() as part of UpdateSession
    }

    return mappingFound.get() ? new UpdateConfigurable(myProject, files) : null;
  }

}

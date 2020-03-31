// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.plugin.idea.tfvc.core;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.microsoft.alm.plugin.context.ServerContext;
import com.microsoft.alm.plugin.external.models.ItemInfo;
import com.microsoft.alm.plugin.external.models.PendingChange;
import com.microsoft.alm.plugin.external.reactive.ReactiveTfvcClientHolder;
import com.microsoft.alm.plugin.external.reactive.ServerIdentification;
import com.microsoft.tfs.model.connector.TfsLocalPath;
import com.microsoft.tfs.model.connector.TfsPath;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * The new, "reactive" TFVC client implementation. Most of the methods are asynchronous and delegate to the external
 * client that is always running. Works with Rd protocol.
 */
public class ReactiveTfvcClient implements TfvcClient {

    private static final Logger ourLogger = Logger.getInstance(ReactiveTfvcClient.class);

    @NotNull
    private final Project myProject;

    public ReactiveTfvcClient(@NotNull Project project) {
        myProject = project;
    }

    private static ServerIdentification getServerIdentification(ServerContext serverContext) {
        return new ServerIdentification(serverContext.getCollectionURI(), serverContext.getAuthenticationInfo());
    }

    @NotNull
    private static <T> CompletionStage<T> traceTime(
            @NotNull String title,
            @NotNull Supplier<CompletionStage<T>> action) {
        long startTime = System.nanoTime();
        return action.get().whenComplete((result, ex) -> {
            long endTime = System.nanoTime();
            double seconds = ((double) endTime - startTime) / 1_000_000_000.0;
            String status = ex == null ? "successfully" : "with error";
            ourLogger.trace(title + " finished " + status + " in " + seconds + " sec");
        });
    }

    @Override
    @NotNull
    public CompletionStage<List<PendingChange>> getStatusForFilesAsync(
            @NotNull ServerContext serverContext,
            @NotNull List<String> pathsToProcess) {
        return traceTime("Status", () -> {
            ServerIdentification serverIdentification = getServerIdentification(serverContext);
            Stream<Path> paths = pathsToProcess.stream().map(Paths::get);

            return ReactiveTfvcClientHolder.getInstance(myProject).getClient()
                    .thenCompose(client -> client.getPendingChangesAsync(serverIdentification, paths));
        });
    }

    @NotNull
    @Override
    public CompletionStage<Void> getLocalItemsInfoAsync(
            @NotNull ServerContext serverContext,
            @NotNull List<String> pathsToProcess,
            @NotNull Consumer<ItemInfo> onItemReceived) {
        return traceTime("Info", () -> {
            ServerIdentification serverIdentification = getServerIdentification(serverContext);
            Stream<Path> paths = pathsToProcess.stream().map(Paths::get);
            return ReactiveTfvcClientHolder.getInstance(myProject).getClient()
                    .thenCompose(client -> client.getItemsInfoAsync(serverIdentification, paths, onItemReceived));
        });
    }

    @NotNull
    @Override
    public CompletionStage<Void> deleteFilesRecursivelyAsync(
            @NotNull ServerContext serverContext,
            @NotNull List<TfsPath> items) {
        return traceTime("Delete", () -> {
            ServerIdentification serverIdentification = getServerIdentification(serverContext);
            return ReactiveTfvcClientHolder.getInstance(myProject).getClient()
                    .thenCompose(client -> client.deleteFilesRecursivelyAsync(serverIdentification, items));
        });
    }

    @NotNull
    @Override
    public CompletionStage<List<TfsLocalPath>> undoLocalChangesAsync(
            @NotNull ServerContext serverContext,
            @NotNull List<TfsPath> items) {
        return traceTime("Undo", () -> {
            ServerIdentification serverIdentification = getServerIdentification(serverContext);
            return ReactiveTfvcClientHolder.getInstance(myProject).getClient()
                    .thenCompose(client -> client.undoLocalChangesAsync(serverIdentification, items));
        });
    }
}

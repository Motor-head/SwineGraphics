package mod.swinegraphics.controller;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.CancellationException;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import javafx.application.Platform;
import mod.swinegraphics.exception.BackgroundException;
import mod.swinegraphics.util.Log;

/**
 * A Multi threading class parameterized by the return type (R) of the
 * asynchronous operation. Includes support for interrupting underlying tasks on
 * timeout.
 *
 * @param <R> The return type of the asynchronous operation that will be
 * assigned.
 */
class BackgroundProcess {

    private static final Long TIMEOUT = 15L;
    private static final TimeUnit UNIT = TimeUnit.SECONDS;
    private static final String INTERRUPT = "Task interrupted";
    private static final String TIME_OUT = "Task timed out";
    private static final String FAILED = "Assigned task failed: ";
    private static final String UNHANDLED = "Unhandled exception";

    private final ExecutorService executor;

    private String error;

    BackgroundProcess() {
        this.executor = Executors.newVirtualThreadPerTaskExecutor();
    }

    /**
     * Assigns an asynchronous operation that takes one parameter, with a
     * default timeout of 15 sec. On timeout, the underlying task will be
     * attempted to be interrupted.
     *
     * @param <T> Input parameter's type.
     * @param <R> Return type.
     * @param function The method reference (Function) representing the
     * asynchronous operation.
     * @param param The single parameter to pass to the function.
     * @param success The method reference (Consumer) which will accept the
     * result of function.
     */
    <T, R> void start(Function<T, R> function, T param, Consumer<R> success, Runnable failure) {
        CompletableFuture<R> process = CompletableFuture.supplyAsync(() -> function.apply(param), executor);
        var handledProcess = handleException(process, failure);
        handledProcess.thenAcceptAsync(r -> {
            success.accept(r);
        }, Platform::runLater);
    }

    /**
     * Assigns an asynchronous operation that takes two parameters, with a
     * default timeout of 15 sec. On timeout, the underlying task will be
     * attempted to be interrupted.
     *
     * @param <T> First input parameter's type.
     * @param <U> Second input parameter's type.
     * @param <R> Return type.
     * @param bifunction The method reference (BiFunction) representing the
     * asynchronous operation.
     * @param param1 First parameter for the BiFunction.
     * @param param2 Second parameter for the BiFunction.
     * @param success The method reference (Consumer) which will accept the
     * result of function.
     * @param failure The method reference (Runnable) which will handle the
     * exception.
     */
    <T, U, R> void start(BiFunction<T, U, R> bifunction, T param1, U param2, Consumer<R> success, Runnable failure) {
        CompletableFuture<R> process = CompletableFuture.supplyAsync(() -> bifunction.apply(param1, param2), executor);
        var handledProcess = handleException(process, failure);
        handledProcess.thenAcceptAsync(r -> {
            success.accept(r);
        }, Platform::runLater);
    }

    /**
     * Shuts down the executor service.
     */
    void shutdown() {
        try {
            if (!executor.isShutdown()) {
                executor.shutdown();
                executor.awaitTermination(TIMEOUT, UNIT);
            }
        } catch (InterruptedException i) {
            Thread.currentThread().interrupt();
            Log.log(i, i.getLocalizedMessage());
        }
    }

    /**
     * Handles the exception thrown by the method.
     *
     * @param <R> Return type of the asynchronous operation.
     * @param process The asynchronous operation which can throw an exception.
     * @param failure The method reference (Runnable) which will handle the
     * exception.
     * @return CompletableFuture.
     */
    private <R> CompletableFuture<R> handleException(CompletableFuture<R> process, Runnable failure) {
        return process.orTimeout(TIMEOUT, UNIT)
                .exceptionally(t -> {
                    String type;
                    switch (t) {
                        case CancellationException c -> {
                            type = FAILED;
                            error = c.getMessage();
                        }
                        case InterruptedException i -> {
                            Thread.currentThread().interrupt();
                            type = INTERRUPT;
                            error = i.getMessage();
                        }
                        case TimeoutException to -> {
                            type = TIME_OUT;
                            error = to.getMessage();
                        }
                        default -> {
                            type = UNHANDLED;
                            error = t.getMessage();
                        }
                    }
                    var exception = new BackgroundException(type, t);
                    Log.log(exception, error);
                    if (failure != null) {
                        Platform.runLater(failure);
                    }
                    throw exception;
                });
    }
}

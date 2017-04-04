package controllers;

import akka.actor.ActorSystem;
import akka.dispatch.Futures;
import akka.pattern.Patterns;
import play.libs.Akka;
import play.libs.F;
import play.mvc.Result;
import scala.compat.java8.FutureConverters;
import scala.concurrent.ExecutionContext;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeoutException;

import static play.mvc.Http.Status.GATEWAY_TIMEOUT;
import static play.mvc.Results.status;

/**
 * A timeout utility class
 */
public class Timeout {

    private final ActorSystem system;

    public Timeout() {
        this.system = Akka.system();
    }

    public Timeout(ActorSystem system) {
        this.system = system;
    }

    public F.Promise<Result> timeoutOrError(F.Function0<Result> function, long timeoutMillis) {
        return timeout(F.Promise.promise(function), timeoutMillis).recover(throwable -> {
            return errorPage();
        });
    }

    public F.Promise<Result> timeoutOrError(F.Promise<Result> promise, long timeoutMillis) {
        return timeout(promise, timeoutMillis).recover(throwable -> {
            return errorPage();
        });
    }

    public Result errorPage() {
        return status(GATEWAY_TIMEOUT, "This page took too long to load");
    }

    public <T> F.Promise<T> timeout(F.Promise<T> inputPromise, long timeout) {
        Future<T> inputFuture = inputPromise.wrapped();
        return F.Promise.wrap(timeout(inputFuture, timeout));
    }

    public <T> CompletionStage<T> timeout(CompletionStage<T> inputStage, long timeout) {
        Objects.requireNonNull(inputStage);
        if (timeout < 0) {
            throw new IllegalArgumentException("Invalid timeout " + timeout);
        }

        // Note that you should add the following to your Build.scala / build.sbt file to use the
        // libraryDependencies += "org.scala-lang.modules" % "scala-java8-compat_2.10" % "0.5.0"
        // You can read more about the java8 compatibility on https://github.com/scala/scala-java8-compat
        Future<T> inputFuture = FutureConverters.toScala(inputStage);
        return FutureConverters.toJava(timeout(inputFuture, timeout));
    }

    public <T> Future<T> timeout(Future<T> inputFuture, long timeout) {
        ExecutionContext ec = system.dispatcher();

        Future<T> failExc = Futures.failed(new TimeoutException("Timeout after " + timeout + " milliseconds"));
        Future<T> timeoutFuture = Patterns.after(Duration.create(timeout, "millis"),
                system.scheduler(), ec,  failExc);

        return Futures.firstCompletedOf(Arrays.asList(inputFuture, timeoutFuture), ec);
    }
}

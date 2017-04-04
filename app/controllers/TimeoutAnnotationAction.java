package controllers;

import play.libs.F;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;

public class TimeoutAnnotationAction extends Action<TimeoutAnnotation> {
    public F.Promise<Result> call(Http.Context ctx) throws Throwable {
        long timeoutMillis = configuration.value();
        play.Logger.info("printing " + timeoutMillis);
        Timeout timeout = new Timeout();
        return timeout.timeoutOrError(delegate.call(ctx), timeoutMillis);
    }
}
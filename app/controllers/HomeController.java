package controllers;

import play.libs.F;
import play.mvc.Controller;
import play.mvc.Result;

/**
 * A controller that uses a timeout if rendering takes too long
 */
public class HomeController extends Controller {

    @TimeoutAnnotation(50)
    public static F.Promise<Result> index() {
        return F.Promise.promise(() -> {
            Thread.sleep(150); // will cause the page rendering to timeout
            return ok(views.html.index.render());
        });
    }
}

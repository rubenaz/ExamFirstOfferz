import io.vertx.core.*;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import org.junit.internal.TextListener;
import org.junit.runner.JUnitCore;

public class MainVerticle extends AbstractVerticle {

  @Override
  public void start(Future<Void> future) {
    HttpServer httpServer = this.vertx.createHttpServer();
    Router router = Router.router(vertx);
    inirRouting(router);

    httpServer.requestHandler(router).listen(8080, httpServerAsyncResult ->
    {
      if (httpServerAsyncResult.succeeded()) {
        future.complete();
      } else {

        future.fail(httpServerAsyncResult.cause());
      }
    });
  }

  private void inirRouting(Router router) {
    router.get("/healthcheck")
      .handler(RouterService::healthCheckHandler);

    router.get("/hello")
      .handler(RouterService::helloHandler);

    router.get("/currentforecasts")
      .handler(RouterService::currentForecastsHandler);

    router.get("/forecasts")
      .handler(RouterService::forecastsHandler);
  }
}
 /* @Override
  public void start(){
    RouterService routerService = new RouterService(vertx);
    //routerService.getResponse();
}
}*/

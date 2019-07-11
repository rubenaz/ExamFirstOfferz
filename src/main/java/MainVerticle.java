import io.vertx.core.*;
import io.vertx.core.file.AsyncFile;
import io.vertx.core.file.OpenOptions;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.web.Router;
import io.vertx.reactivex.core.parsetools.RecordParser;
import io.vertx.rxjava.core.buffer.Buffer;
import io.vertx.rxjava.core.parsetools.JsonParser;
import io.vertx.rxjava.core.streams.Pump;
import io.vertx.rxjava.core.streams.ReadStream;
import org.json.JSONObject;
import org.junit.experimental.theories.DataPoint;
import org.junit.internal.TextListener;
import org.junit.runner.JUnitCore;

import java.util.concurrent.atomic.AtomicInteger;

import static io.vertx.core.parsetools.JsonEventType.VALUE;

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
//================================================================================================

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

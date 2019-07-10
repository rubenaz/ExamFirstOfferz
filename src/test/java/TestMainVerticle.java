import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * This is our JUnit test for our verticle. The test uses vertx-unit, so we declare a custom runner.
 */
@RunWith(VertxUnitRunner.class)
public class TestMainVerticle {

  private Vertx vertx;
  private Integer port;

  /**
   * Before executing our test, let's deploy our verticle.
   * <p/>
   * This method instantiates a new Vertx and deploy the verticle. Then, it waits in the verticle has successfully
   * completed its start sequence (thanks to `context.asyncAssertSuccess`).
   *
   * @param context the test context.
   */
  @Before
  public void setUp(TestContext context) throws IOException {
    vertx = Vertx.vertx();

    // Let's configure the verticle to listen on the 'test' port (randomly picked).
    // We create deployment options and set the _configuration_ json object:
    ServerSocket socket = new ServerSocket(0);
    port = 8080;
    socket.close();

    DeploymentOptions options = new DeploymentOptions()
      .setConfig(new JsonObject().put("http.port", port)
      );

    // We pass the options as the second parameter of the deployVerticle method.
    vertx.deployVerticle(MainVerticle.class.getName(), options, context.asyncAssertSuccess());
  }

  /**
   * This method, called after our test, just cleanup everything by closing the vert.x instance
   *
   * @param context the test context
   */
  @After
  public void tearDown(TestContext context) {
    vertx.close(context.asyncAssertSuccess());
  }

  /**
   * Let's ensure that our application behaves correctly.
   *
   * @param context the test context
   */
  @Test
  public void testHealtCheck(TestContext context) {
    final Async async = context.async();
    vertx.createHttpClient().getNow(port, "localhost", "/healthcheck", response -> {
      response.handler(body -> {
        context.assertTrue(body.toString().contains("I'm alive!!!"));
        async.complete();
      });
    });
  }
  @Test
  public void testHello(TestContext context) {
    final Async async = context.async();
    vertx.createHttpClient().getNow(port, "localhost", "/hello?name=Tal", response -> {
      response.handler(body -> {
        context.assertTrue(body.toString().contains("Hello Tal!"));
        async.complete();
      });
    });
  }

  /*@Test
  public void checkThatTheIndexPageIsServed(TestContext context) {
    Async async = context.async();
    vertx.createHttpClient().getNow(port, "localhost", "/assets/index.html", response -> {
      context.assertEquals(response.statusCode(), 200);
      context.assertEquals(response.headers().get("content-type"), "text/html;charset=UTF-8");
      response.bodyHandler(body -> {
        context.assertTrue(body.toString().contains("<title>My Whisky Collection</title>"));
        async.complete();
      });
    });
  }*/

  @Test
  public void checkThatWeCanAdd(TestContext context) {
    Async async = context.async();
    final String json = Json.encodePrettily(new Whisky("Jameson", "Ireland"));
    vertx.createHttpClient().post(port, "localhost", "/api/whiskies")
      .putHeader("content-type", "application/json")
      .putHeader("content-length", Integer.toString(json.length()))
      .handler(response -> {
        context.assertEquals(response.statusCode(), 201);
        context.assertTrue(response.headers().get("content-type").contains("application/json"));
        response.bodyHandler(body -> {
          final Whisky whisky = Json.decodeValue(body.toString(), Whisky.class);
          context.assertEquals(whisky.getName(), "Jameson");
          context.assertEquals(whisky.getOrigin(), "Ireland");
          context.assertNotNull(whisky.getId());
          async.complete();
        });
      })
      .write(json)
      .end();
  }
}

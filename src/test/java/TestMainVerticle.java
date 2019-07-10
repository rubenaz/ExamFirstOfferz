import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.json.HTTP;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.ServerSocket;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;


@RunWith(VertxUnitRunner.class)
public class TestMainVerticle {

  private Vertx vertx;
  private Integer port;

  @Before
  public void setUp(TestContext context) throws IOException {
    vertx = Vertx.vertx();

    ServerSocket socket = new ServerSocket(0);
    port = 8080;
    socket.close();

    DeploymentOptions options = new DeploymentOptions()
      .setConfig(new JsonObject().put("http.port", port)
      );

    // We pass the options as the second parameter of the deployVerticle method.
    vertx.deployVerticle(MainVerticle.class.getName(), options, context.asyncAssertSuccess());
  }


  @After
  public void tearDown(TestContext context) {
    vertx.close(context.asyncAssertSuccess());
  }


  @Test
  public void testHealtCheck(TestContext context) {
    final Async async = context.async();
    vertx.createHttpClient().getNow(port, "localhost", "/healthcheck", response -> {
      context.assertEquals(response.statusCode(), HttpResponseStatus.OK.code());
      context.assertEquals(response.headers().get("content-type"), "text/plain");
      response.handler(body -> {
        context.assertTrue(body.toString().contains("I'm alive!!!"));
        async.complete();
      });
    });
  }
  @Test
  public void testHealtCheckWrongURL(TestContext context) {
    final Async async = context.async();
    vertx.createHttpClient().getNow(port, "localhost", "/healthchecksacdf", response -> {
      context.assertEquals(response.statusCode(), HttpResponseStatus.NOT_FOUND.code());
      response.handler(body -> {
        async.complete();
      });
    });
  }
  @Test
  public void testHello(TestContext context) {
    final Async async = context.async();
    final String name ="Tal";
    vertx.createHttpClient().getNow(port, "localhost", "/hello?name=" +name , response -> {
      context.assertEquals(response.statusCode(), HttpResponseStatus.OK.code());
      context.assertEquals(response.headers().get("content-type"), "text/plain");
      response.handler(body -> {
        context.assertTrue(body.toString().contains("Hello " + name + "!"));
        async.complete();
      });
    });
  }
  @Test
  public void testHelloWrongURL(TestContext context) {//without / before hello
    final Async async = context.async();
    vertx.createHttpClient().getNow(port, "localhost", "hello?name=Tal", response -> {
      context.assertEquals(response.statusCode(), HttpResponseStatus.NOT_FOUND.code());
      response.handler(body -> {
        async.complete();
      });
    });
  }
  @Test
  public void testCurrentWeather(TestContext context) {
    final  String date = getDate(0);
    final  String city = "paris";
    final  String country="fr";
    final Async async = context.async();
    vertx.createHttpClient().getNow(port, "localhost", "/currentforecasts?city="+city+"&country=" +country, response -> {
      context.assertEquals(response.statusCode(), HttpResponseStatus.OK.code());
      context.assertEquals(response.headers().get("content-type"), "application/json");
      response.handler(body -> {
        final JSONObject currentWeather = new JSONObject(body.toString());
        context.assertEquals(currentWeather.get("country"), country);
        context.assertEquals(currentWeather.get("city"), city);
        context.assertEquals(currentWeather.get("date"), date);
        async.complete();
      });
    });
  }

  @Test
  public void testCurrentWeatherWrongCity(TestContext context) {
    final Async async = context.async();
    final String city = "juju";
    final  String country="fr";
    vertx.createHttpClient().getNow(port, "localhost", "/currentforecasts?city="+city+"&country=" +country, response -> {
      context.assertEquals(response.statusCode(), HttpResponseStatus.BAD_REQUEST.code());
      context.assertEquals(response.headers().get("content-type"), "application/json");
      response.handler(body -> {
        final JSONObject currentWeather = new JSONObject(body.toString());
        context.assertEquals(currentWeather.get("error"), "this place is not exist");
        async.complete();
      });
    });
  }

  @Test
  public void testCurrentWeatherWrongCountry(TestContext context) {
    final Async async = context.async();
    final  String city = "paris";
    final String country="es";
    vertx.createHttpClient().getNow(port, "localhost", "/currentforecasts?city="+city+"&country=" +country, response -> {
      context.assertEquals(response.statusCode(), HttpResponseStatus.BAD_REQUEST.code());
      context.assertEquals(response.headers().get("content-type"), "application/json");
      response.handler(body -> {
        final JSONObject currentWeather = new JSONObject(body.toString());
        context.assertEquals(currentWeather.get("error"), "this place is not exist");
        async.complete();
      });
    });
  }
  @Test
  public void testCurrentWeatherCountryTooLong(TestContext context) {
    final Async async = context.async();
    final String city = "paris";
    final String country="france";
    vertx.createHttpClient().getNow(port, "localhost", "/currentforecasts?city="+city+"&country=" +country, response -> {
      context.assertEquals(response.statusCode(), HttpResponseStatus.BAD_REQUEST.code());
      context.assertEquals(response.headers().get("content-type"), "application/json");
      response.handler(body -> {
        final JSONObject currentWeather = new JSONObject(body.toString());
        context.assertEquals(currentWeather.get("error"), "GIVE A COUNTRY PARAM WITH 2 LETTERS");
        async.complete();
      });
    });
  }
  @Test
  public void testCurrentWeatherWrongURL(TestContext context) {
    final Async async = context.async();
    vertx.createHttpClient().getNow(port, "localhost", "/currentforecastscity=paris&country=france", response -> {
      context.assertEquals(response.statusCode(), HttpResponseStatus.NOT_FOUND.code());
      response.handler(body -> {
        async.complete();
      });
    });
  }

  @Test
  public void testForecastWeather(TestContext context) {

    final Async async = context.async();
    int days= 3;
    final String city="Begichevo";
    final String country="RU";
    vertx.createHttpClient().getNow(port, "localhost", "/forecasts?city="+city+"&country="+country+"&days="+days, response -> {
      context.assertEquals(response.statusCode(), HttpResponseStatus.OK.code());
      context.assertEquals(response.headers().get("content-type"), "application/json");
      response.handler(body -> {
        final JSONObject currentWeather = new JSONObject(body.toString());
        JSONArray forecast =currentWeather.getJSONArray("forecasts");
        context.assertEquals(forecast.length(),3);
        for (int i = 0; i < 3 ; i++) {
          System.out.println(forecast.getJSONObject(i).get("date"));
          context.assertEquals(forecast.getJSONObject(i).get("date"), getDate(i));

        }
        async.complete();
      });
    });
  }

  @Test
  public void testForecastWeatherDaysIsNotNumber(TestContext context) {
    final Async async = context.async();
    final String days= "paris";
    final String city="Begichevo";
    final String country="RU";
    vertx.createHttpClient().getNow(port, "localhost", "/forecasts?city="+city+"&country="+country+"&days="+days, response -> {
      context.assertEquals(response.statusCode(), HttpResponseStatus.BAD_REQUEST.code());
      response.handler(body -> {
        async.complete();
      });
    });
  }
  @Test
  public void testForecastWeatherPassDayLimit(TestContext context) {

    final Async async = context.async();
    final int days= 89;
    final String city="Begichevo";
    final String country="RU";
    vertx.createHttpClient().getNow(port, "localhost", "/forecasts?city="+city+"&country="+country+"&days="+days, response -> {
      context.assertEquals(response.statusCode(), HttpResponseStatus.BAD_REQUEST.code());
      context.assertEquals(response.headers().get("content-type"), "application/json");
      response.handler(body -> {
        final JSONObject currentWeather = new JSONObject(body.toString());
        context.assertEquals(currentWeather.get("error"), "DAYS INCORRECT");
        async.complete();
      });
    });
  }
  @Test
  public void testForecastWeatherNegativeDay(TestContext context) {

    final Async async = context.async();
    final int days= -2;
    final String city="Begichevo";
    final String country="RU";
    vertx.createHttpClient().getNow(port, "localhost", "/forecasts?city="+city+"&country="+country+"&days="+days, response -> {
      context.assertEquals(response.statusCode(), HttpResponseStatus.BAD_REQUEST.code());
      context.assertEquals(response.headers().get("content-type"), "application/json");
      response.handler(body -> {
        final JSONObject currentWeather = new JSONObject(body.toString());
        context.assertEquals(currentWeather.get("error"), "DAYS INCORRECT");
        async.complete();
      });
    });
  }
  @Test
  public void testForecastWeatherUpperCaseCity(TestContext context) {

    final Async async = context.async();
    final int day= 2;
    final String city="BEGICHEVO";
    final String country="RU";
    vertx.createHttpClient().getNow(port, "localhost", "/forecasts?city="+city+"&country="+country+"&days="+day, response -> {
      context.assertEquals(response.statusCode(), HttpResponseStatus.OK.code());
      context.assertEquals(response.headers().get("content-type"), "application/json");
      response.handler(body -> {
        final JSONObject currentWeather = new JSONObject(body.toString());
        JSONArray forecast =currentWeather.getJSONArray("forecasts");
        context.assertEquals(forecast.length(),day);
        for (int i = 0; i < day ; i++) {
          System.out.println(forecast.getJSONObject(i).get("date"));
          context.assertEquals(forecast.getJSONObject(i).get("date"), getDate(i));

        }
        async.complete();
      });
    });
  }
  @Test
  public void testForecastWeatherLowerCaseCity(TestContext context) {

    final Async async = context.async();
    final int day= 2;
    final String city="Begichevo";
    final String country="ru";
    vertx.createHttpClient().getNow(port, "localhost", "/forecasts?city="+city+"&country="+country+"&days="+day, response -> {
      context.assertEquals(response.statusCode(), HttpResponseStatus.OK.code());
      context.assertEquals(response.headers().get("content-type"), "application/json");
      response.handler(body -> {
        final JSONObject currentWeather = new JSONObject(body.toString());
        JSONArray forecast =currentWeather.getJSONArray("forecasts");
        context.assertEquals(forecast.length(),day);
        for (int i = 0; i < day ; i++) {
          System.out.println(forecast.getJSONObject(i).get("date"));
          context.assertEquals(forecast.getJSONObject(i).get("date"), getDate(i));

        }
        async.complete();
      });
    });
  }

  private String getDate(int days)
  {
    LocalDate localDate = LocalDate.now();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    return localDate.plusDays(days).format(formatter);
  }

}

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RouterService extends AbstractVerticle {

  public static void forecastsHandler(RoutingContext routingContext) {
    String city = routingContext.request()
      .getParam("city");

    String country = routingContext.request()
      .getParam("country");

    String days = routingContext.request()
      .getParam("days");

    JSONObject response = ApiService.getWeatherPerDays(city, country,days);
    if( response.has("error"))
      routingContext.response()
        .putHeader("content-type", "application/json")
        .setStatusCode(400)
        .end(response.toString());
    else {
      JSONObject forecasts = getFinalResponsePerDay(response, days);
      System.out.println("forecasts: " + forecasts);
      routingContext.response()
        .putHeader("content-type", "application/json")
        .setStatusCode(200)
        .end(forecasts.toString());
    }
  }
  public static void currentForecastsHandler(RoutingContext routingContext) {
    String city = routingContext.request()
      .getParam("city");

    String country = routingContext.request()
      .getParam("country");

    JSONObject response = ApiService.getCurrentWheater(city, country);

    System.out.println("response: " + response);
    if(    response.has("error")) {

      System.out.println("why im here???: ");

      routingContext.response()
        .putHeader("content-type", "application/json")
        .setStatusCode(400)
        .end(response.toString());
    }
    else {
      System.out.println("currentForecasts: ");

      JSONObject currentForecasts = getFinalResponse(response, city, country);

      routingContext.response()
        .putHeader("content-type", "application/json")
        .setStatusCode(200)
        .end(currentForecasts.toString());
    }
  }

  public static void helloHandler(RoutingContext routingContext) {
    String name = routingContext.request()
      .getParam("name");

    routingContext.response()
      .putHeader("content-type", "text/plain")
      .setStatusCode(200)
      .end("Hello " + name + "!");
  }

  public static void healthCheckHandler(RoutingContext routingContext) {
    routingContext.response()
      .putHeader("content-type", "text/plain")
      .setStatusCode(200)
      .end("I'm alive!!!");
  }


  private static JSONObject getFinalResponse(JSONObject result, String cityName, String countryName) {
    String date = result.get("dt").toString();
    date = getDate(Long.parseLong(date) * 1000);
    JSONObject json = new JSONObject();
    json.put("country", countryName);
    json.put("city", cityName);
    json.put("temp", result.getJSONObject("main").get("temp").toString());
    json.put("humidity", result.getJSONObject("main").get("humidity").toString());
    json.put("date", date);
    return json;

  }
  public static String getDate(long seconds) {
    System.out.println(seconds);
    Date date = new Date(seconds);
    DateFormat format = new SimpleDateFormat("dd/MM/yyyy");
    return format.format(date);
  }
  private static JSONObject getFinalResponsePerDay(JSONObject result, String days) {
    System.out.println("IN THE GET FINAL RESPONSE");
    int count = 0;
    double temp = 0, temp_max = 0, temp_min = 0;
    int countByThreeHours = 0;
    int i = 0;
    int numOfDays = Integer.parseInt(days);
    String apiDate = result.getJSONArray("list").getJSONObject(0).get("dt_txt").toString().split(" ")[0];
    JSONObject json = new JSONObject();
    JSONArray weatherArray = new JSONArray();
    while (count < numOfDays) {
      JSONObject data = result.getJSONArray("list").getJSONObject(i);
      String currentDate = data.get("dt_txt").toString().split(" ")[0];
      if (!apiDate.equals(currentDate)) {
        weatherArray.put(createDayObject(apiDate, temp, temp_max, temp_min, countByThreeHours));
        count++;
        countByThreeHours = 0;
        apiDate = currentDate;
        temp = 0;
        temp_max = 0;
        temp_min = 0;
      }
      temp += Double.parseDouble(data.getJSONObject("main").get("temp").toString());
      temp_min += Double.parseDouble(data.getJSONObject("main").get("temp_min").toString());
      temp_max += Double.parseDouble(data.getJSONObject("main").get("temp_max").toString());
      countByThreeHours++;
      i++;
    }
    json.put("forecasts", weatherArray);
    return json;
  }
  private static JSONObject createDayObject(String apiDate, double temp, double temp_max, double temp_min, int countByThreeHours) {
    JSONObject day = new JSONObject();
    day.put("date", apiDate);
    day.put("temp", String.format("%.2f", temp / countByThreeHours));
    day.put("temp_max", String.format("%.2f", temp_max / countByThreeHours));
    day.put("temp_min", String.format("%.2f", temp_min / countByThreeHours));
    return day;
  }















}

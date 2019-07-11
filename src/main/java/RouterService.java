import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.file.AsyncFile;
import io.vertx.core.file.OpenOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.web.RoutingContext;
import io.vertx.rxjava.core.streams.Pump;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

public class RouterService extends AbstractVerticle {

  public static void forecastsHandler(RoutingContext routingContext) {
    //  if the url is type of "http://localhost:8080/forecasts?city=XXX&country=XX&days=X"

    String city = routingContext.request()//get the city parameter
      .getParam("city");
    city=city.substring(0,1).toUpperCase() + city.substring(1).toLowerCase();

    String country = routingContext.request()//get the country parameter
      .getParam("country");
    country=country.toUpperCase();

    String days = routingContext.request()//get the days parameter
      .getParam("days");
    Future<String> future = readFile(routingContext.vertx(),city,country);//get the id of the city.json file
    future.setHandler(asyncResult -> {
      if(asyncResult.succeeded())
      {
        JSONObject response = ApiService.getWeatherPerDays(days,future.result());//get the response from api
        if( response.has("error"))// if find error (city not correct bug with internet etc...)
          routingContext.response()
            .putHeader("content-type", "application/json")
            .setStatusCode(400)
            .end(response.toString());
        else {
          JSONObject forecasts = getFinalResponsePerDay(response, days);//get the final response in json object
          routingContext.response()
            .putHeader("content-type", "application/json")
            .setStatusCode(200)
            .end(forecasts.toString());
        }
    }
      else
      {
        routingContext.response()
          .putHeader("content-type", "application/json")
          .setStatusCode(400)
          .end(future.result());
      }
  });
  }
//================================================================================================

  public static void currentForecastsHandler(RoutingContext routingContext) {
    //  if the url is type of "http://localhost:8080/currentforecasts?city=XXX&country=XX"

    String city = routingContext.request()//get the city parameter
      .getParam("city");

    String country = routingContext.request()//get the country parameter
      .getParam("country");

    JSONObject response = ApiService.getCurrentWheater(city, country);//get response from api

    if (response.has("error")) {// if find error (city not correct bug with internet etc...)
      routingContext.response()
        .putHeader("content-type", "application/json")
        .setStatusCode(400)
        .end(response.toString());
    } else {
      JSONObject currentForecasts = getFinalResponse(response, city, country);//get the final result in json object

      routingContext.response()
        .putHeader("content-type", "application/json")
        .setStatusCode(200)
        .end(currentForecasts.toString());
    }
  }
//================================================================================================

  public static void helloHandler(RoutingContext routingContext) {
    //   if the url is type of "http://localhost:8080/hello?name=XX"
    String name = routingContext.request()//get the name parameter
      .getParam("name");

    routingContext.response()
      .putHeader("content-type", "text/plain")
      .setStatusCode(200)
      .end("Hello " + name + "!");
  }
//================================================================================================

  public static void healthCheckHandler(RoutingContext routingContext) {
    //   if the url is type of "http://localhost:8080/healthcheck"

    routingContext.response()
      .putHeader("content-type", "text/plain")
      .setStatusCode(200)
      .end("I'm alive!!!");
  }
//================================================================================================

  private static JSONObject getFinalResponse(JSONObject result, String cityName, String countryName) {
    //create JsonObject for current Weather
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
//================================================================================================

  private static String getDate(long seconds) {
    //transalte the date that appeared in second in the api to date
    Date date = new Date(seconds);
    DateFormat format = new SimpleDateFormat("yyyy/MM/dd");
    return format.format(date);
  }
//================================================================================================

  private static JSONObject getFinalResponsePerDay(JSONObject result, String days) {
    //create JsonObject for  Weather per days
    int countOfDays = 0;//how many day passed
    double temp = 0, temp_max = 0, temp_min = 0; // the current temperature , max and min
    int countByThreeHours = 0;//to know how to calculate the average of the temp ( count of 3 hours per day )
    int i = 0;
    int numOfDaysInTheUrl = Integer.parseInt(days);
    String apiDate = result.getJSONArray("list").getJSONObject(0).get("dt_txt").toString().split(" ")[0];//the date fron the api
    JSONObject json = new JSONObject();
    JSONArray weatherArray = new JSONArray();
    while (countOfDays < numOfDaysInTheUrl) {//
      JSONObject data = result.getJSONArray("list").getJSONObject(i);
      String currentDate = data.get("dt_txt").toString().split(" ")[0];
      if (!apiDate.equals(currentDate)) {//if the date changed (if it is the next day )
        weatherArray.put(createDayObject(apiDate, temp, temp_max, temp_min, countByThreeHours));//create JsonObject of this day
        countOfDays++;//next day
        countByThreeHours = 0;
        apiDate = currentDate;
        temp = 0;
        temp_max = 0;
        temp_min = 0;
      }
      temp += Double.parseDouble(data.getJSONObject("main").get("temp").toString());//add the temp for this 3 hours
      temp_min += Double.parseDouble(data.getJSONObject("main").get("temp_min").toString());//add the temp for this 3 hours
      temp_max += Double.parseDouble(data.getJSONObject("main").get("temp_max").toString());//add the temp for this 3 hours
      countByThreeHours++;
      i++;
    }
    json.put("forecasts", weatherArray);
    return json;
  }

  private static JSONObject createDayObject(String apiDate, double temp, double temp_max, double temp_min, int countByThreeHours) {
    JSONObject day = new JSONObject();
    day.put("date", apiDate.replace("-","/"));
    day.put("temp", String.format("%.2f", temp / countByThreeHours));//average  temp
    day.put("temp_max", String.format("%.2f", temp_max / countByThreeHours));//average temp max
    day.put("temp_min", String.format("%.2f", temp_min / countByThreeHours));//average temp min
    return day;
  }

  private static Future<String> readFile(Vertx vertx, String city, String country) {
    //read fron city.json file
    Future<String> future = Future.future();
    vertx.fileSystem().readFile("city.json", result -> {
      if (result.succeeded()) {
        String id=ApiService.ID_NOT_FOUND;
        JsonArray listname = result.result().toJsonArray();
        for (int i = 0; i < listname.size(); i++) {
          if (listname.getJsonObject(i).getValue("name").toString().equals(city) && listname.getJsonObject(i).getValue("country").toString().equals(country)) {
            id=listname.getJsonObject(i).getValue("id").toString();
          }
        }
        future.complete(id);
      } else {
        future.fail(result.cause());
      }
    });
    return future;
  }
}

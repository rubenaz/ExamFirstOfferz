import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.http.HttpMethod;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import sun.misc.IOUtils;
import sun.security.provider.certpath.OCSPResponse;

import javax.xml.ws.Response;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

public class ApiService {

  final static String KEY = "976136126d553c05d8890ac35365a99f";
  final static String WEATHER_API = "https://api.openweathermap.org/data/2.5/weather?q=";
  final static String WEATHER_PER_DAY_API = "https://api.openweathermap.org/data/2.5/forecast?";

  ApiService() {
  }


//================================================================================================

  public static JSONObject getCurrentWheater(String city, String country) {
    if (country.length() > 2) {
      System.out.println("AH DUDDIII");
      return errorMessage("GIVE A COUNTRY PARAM WITH 2 LETTERS");
    }
    String url = WEATHER_API + city + "," + country + "&units=metric&APPID=" + KEY;
    try {
      return getRequest(url);
    } catch (IOException e) {
      e.printStackTrace();
      return errorMessage("failed to retrieve weather");
    }
  }

//================================================================================================

  public static JSONObject getWeatherPerDays(String city, String country, String days) {
    String id = getTheID(city, country);
    if (id.equals("not found"))
      return errorMessage("GIVE A CORRECT CITY/COUNTRY");
    if (days.length() > 1 || days.equals("") || Integer.parseInt(days) == 0 || Integer.parseInt(days) > 5)
      return errorMessage("DAYS INCORRECT");
    String url = WEATHER_PER_DAY_API + "id=" + id + "&units=metric&APPID=" + KEY;
    try {
      return getRequest(url);
    } catch (IOException e) {
      e.printStackTrace();
      return errorMessage("failed to retrieve weather");
    }
  }

  //================================================================================================
  private static JSONObject getRequest(String urlApi) throws IOException {//flag =0 currentCall flag=1 perdayCall
    URL url = new URL(urlApi);
    HttpURLConnection con = (HttpURLConnection) url.openConnection();

    con.setRequestMethod(HttpMethod.GET.name());

    if (con.getResponseCode() > HttpResponseStatus.OK.code()) {
      if (con.getResponseCode() == HttpResponseStatus.NOT_FOUND.code())
        return errorMessage("this place is not exist");
      else
        return errorMessage("error " + con.getResponseCode());
    }

    BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
    String inputLine;
    StringBuilder content = new StringBuilder();
    while ((inputLine = in.readLine()) != null) {
      content.append(inputLine);
    }
    in.close();

    return new JSONObject(content.toString());
  }

  //================================================================================================
  private static JSONObject errorMessage(String message) {
    JSONObject json = new JSONObject();
    json.put("error", message);
    return json;
  }

  //================================================================================================
  private static String getTheID(String city, String country) {
    System.out.println("AHAHAHAHAHAHAAHAHAH");
    Gson gson = new Gson();
    JsonElement json = null;
    try {
      json = gson.fromJson(new FileReader("city.json"), JsonElement.class);
      JsonArray cityArray = json.getAsJsonArray();
      for (int i = 0; i < cityArray.size(); i++) {
        String place = cityArray.get(i).getAsJsonObject().get("name").toString().replace("\"", "");
        String rac = cityArray.get(i).getAsJsonObject().get("country").toString().replace("\"", "");
        city = city.substring(0, 1).toUpperCase() + city.substring(1);
        if (place.equals(city) && rac.equals(country.toUpperCase()))
          return cityArray.get(i).getAsJsonObject().get("id").toString();
      }
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    return "not found";
  }
}


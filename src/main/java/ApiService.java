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
  final static String ID_NOT_FOUND = "id not found";
  final static String INCORRECT_DAY_MESSAGE = "DAYS INCORRECT";
  final static String INCORRECT_PLACE_MESSAGE = "this place is not exist";
  final static String INCORRECT_PLACE_SIZE_MESSAGE = "GIVE A COUNTRY PARAM WITH 2 LETTERS";
  final static String INCORRECT_URL_MESSAGE = "failed to retrieve weather";
  final static String ERROR_VARIABLE = "error";

  ApiService() {
  }


//================================================================================================

  public static JSONObject getCurrentWheater(String city, String country) {
    //get request api to get the current weather
    if (country.length() > 2) {//if the size of the country is a complete name and not two-letter country code
      return errorMessage(INCORRECT_PLACE_SIZE_MESSAGE);
    }
    //url of the request api
    String url = WEATHER_API + city + "," + country + "&units=metric&APPID=" + KEY;
    try {
      return getRequest(url);
    } catch (IOException e) {
      e.printStackTrace();
      return errorMessage(INCORRECT_URL_MESSAGE);
    }
  }

//================================================================================================

  public static JSONObject getWeatherPerDays(String days, String id) {
    //get request api to get the weather per day
    if (id.equals(ID_NOT_FOUND))// if the id not found in the json file
      return errorMessage(INCORRECT_PLACE_MESSAGE);
    if (days.length() > 1 || days.equals("") || Integer.parseInt(days) == 0 || Integer.parseInt(days) > 5)//if is not a correct day period
      return errorMessage(INCORRECT_DAY_MESSAGE);
    //the url of the request api
    String url = WEATHER_PER_DAY_API + "id=" + id + "&units=metric&APPID=" + KEY;
    try {
      return getRequest(url);
    } catch (IOException e) {
      e.printStackTrace();
      return errorMessage(INCORRECT_URL_MESSAGE);
    }
  }

  //================================================================================================
  private static JSONObject getRequest(String urlApi) throws IOException {
    //create the request api
    URL url = new URL(urlApi);
    HttpURLConnection con = (HttpURLConnection) url.openConnection();

    con.setRequestMethod(HttpMethod.GET.name());

    if (con.getResponseCode() > HttpResponseStatus.OK.code()) {
      if (con.getResponseCode() == HttpResponseStatus.NOT_FOUND.code())
        return errorMessage(INCORRECT_PLACE_MESSAGE);
      else
        return errorMessage(ERROR_VARIABLE + con.getResponseCode());
    }

    BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
    String inputLine;
    StringBuilder content = new StringBuilder();
    while ((inputLine = in.readLine()) != null) {
      content.append(inputLine);
    }
    in.close();

    return new JSONObject(content.toString());//return the response in json type
  }

  //================================================================================================
  private static JSONObject errorMessage(String message) {//create the error message
    JSONObject json = new JSONObject();
    json.put(ERROR_VARIABLE, message);
    return json;
  }
}


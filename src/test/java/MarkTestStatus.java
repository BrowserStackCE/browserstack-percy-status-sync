import okhttp3.*;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.annotations.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.Properties;

public class MarkTestStatus {
    @Test
    public void test() throws Exception {
        String status = getPercyBuildStatus();

        if(!status.equals("unreviewed")){
            setSessionStatus("passed","Percy Build has been successfully completed!");
        }else if(status.contains("forbidden")){
            setSessionStatus("failed","Something went wrong! Check if Percy Token and Build Id is correct. API Status: "+status);
        }else{
            setSessionStatus("failed","Percy build has unreviewed snapshots!");
        }
    }
    public String getPercyBuildStatus() throws IOException {
        JSONObject dataJSON=null, attributesJSON=null;
        String buildID = System.getProperty("buildid");

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("https://percy.io/api/v1/builds/"+buildID)
                .get()
                .addHeader("Authorization", "Token "+System.getenv("PERCY_TOKEN"))
                .build();

        Response response = client.newCall(request).execute();

        JSONObject apiResponseJSON = new JSONObject(response.body().string());
        try{
            dataJSON = new JSONObject(apiResponseJSON.get("data").toString());
            attributesJSON = new JSONObject(dataJSON.get("attributes").toString());
            System.out.println("State:"+attributesJSON.get("review-state").toString());
            return attributesJSON.get("review-state").toString();
        }catch (JSONException jsonException){
            System.out.println("Something went wrong! Check if Percy Token and Build Id is correct. API Status: "+apiResponseJSON.get("errors"));
            return apiResponseJSON.get("errors").toString();
        }
    }
    public void setSessionStatus(String status, String reason) throws Exception{
        String sessionID = "";
        try (InputStream input = new FileInputStream("src/test/resources/session-data/currentSessionID.properties")) {
            Properties prop = new Properties();
            prop.load(input);
            sessionID = prop.getProperty("sessionId");
            System.out.println(sessionID);

        } catch (IOException ex) {
            ex.printStackTrace();
        }

        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse("text/plain");
        RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("status",status)
                .addFormDataPart("reason",reason)
                .build();
        Request request = new Request.Builder()
                .url("https://api.browserstack.com/automate/sessions/"+sessionID+".json")
                .method("PUT", body)
                .addHeader("Authorization", basicAuthHeaderGeneration())
                .build();
        Response response = client.newCall(request).execute();
    }
    public String basicAuthHeaderGeneration(){
        String username = System.getenv("BROWSERSTACK_USERNAME");
        String accessKey = System.getenv("BROWSERSTACK_ACCESS_KEY");
        String authCreds = username+":"+accessKey;
        return "Basic " + Base64.getEncoder().encodeToString(authCreds.getBytes());
    }
}

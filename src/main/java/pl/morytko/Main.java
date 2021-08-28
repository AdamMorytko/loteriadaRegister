package pl.morytko;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {
    private static String file ="src/main/resources/codes.txt";
    private static String email = "";
    private static String password = "";

    public static void main(String[] args) throws IOException, ParseException {
        CloseableHttpClient client = HttpClients.createDefault();
        String sessionToken = login(client, email, password);
        List<String> codesList = readCodesFromFileToArrayList();
        codesList.forEach(code -> {
            try {
                registerCode(client, sessionToken, code);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        client.close();
    }

    private static List<String> readCodesFromFileToArrayList()
            throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        Stream<String> stringStream = reader.lines();
        return stringStream.collect(Collectors.toList());
    }

    private static String login(CloseableHttpClient client, String email, String password) throws IOException, ParseException {
        HttpPost httpPost = new HttpPost("https://api.loteriada.pl/api/login");
        String json = String.format("{\"f_email\":\"%s\",\"f_pass\":\"%s\"}", email, password);
        StringEntity entity = new StringEntity(json);
        httpPost.setEntity(entity);
        CloseableHttpResponse response = client.execute(httpPost);
        InputStream inputStream = response.getEntity().getContent();
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject = (JSONObject) jsonParser.parse(new InputStreamReader(inputStream, "UTF-8"));
        return (String) jsonObject.get("sessionToken");
    }

    private static void registerCode
            (CloseableHttpClient client, String token, String code) throws IOException {
        HttpPost httpPost = new HttpPost("https://api.loteriada.pl/api/registerCode");
        String json = String.format("{\"code\":\"%s\",\"token\":\"%s\"}", code, token);
        StringEntity entity = new StringEntity(json);
        httpPost.setEntity(entity);
        CloseableHttpResponse response = client.execute(httpPost);
        System.out.println("Kod: " + code + " - odpowiedź: " + EntityUtils.toString(response.getEntity()));
    }
}


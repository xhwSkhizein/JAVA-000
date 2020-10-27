import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

public class OkhttpClient {

    public static void main(String[] args) {
        // https://square.github.io/okhttp/
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("http://localhost:8801")
                .build();

        try {
            Response response = client.newCall(request).execute();
            String result = response.body().string();
            System.out.println(result);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
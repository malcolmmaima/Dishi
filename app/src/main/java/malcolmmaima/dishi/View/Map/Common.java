package malcolmmaima.dishi.View.Map;

import malcolmmaima.dishi.View.Map.Remote.IGoogleApi;
import malcolmmaima.dishi.View.Map.Remote.RetrofitClient;

public class Common {
    public static  final String baseURL = "https://googleapis.com";

    public static IGoogleApi getGoogleApi(){

        return RetrofitClient.getClient(baseURL).create(IGoogleApi.class);
    }
}

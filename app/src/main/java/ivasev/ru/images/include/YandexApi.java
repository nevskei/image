package ivasev.ru.images.include;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;

import ivasev.ru.images.*;

public class YandexApi {

    private static SharedPreferences sharedPrefs;
    private static SharedPreferences.Editor editor = null;

    private static String token = "";

    private static String apiId = "415b22ca0af04265bbc99c8f6dea34b6";

    private static String url = "yx415b22ca0af04265bbc99c8f6dea34b6";

    public static final String myPrefs = "yandexpref";
    public static final String nameKey = "apiKey";

    public static String getToken(AppCompatActivity cntxt ) {

        Intent intent = cntxt.getIntent();

        sharedPrefs = cntxt.getSharedPreferences(myPrefs, Context.MODE_PRIVATE);
        editor = sharedPrefs.edit();

        if (sharedPrefs.contains(nameKey)) {
            token = sharedPrefs.getString(nameKey, "");
        }

        if (token.isEmpty()) {
            intent = new Intent(cntxt, YandexActivity.class);
            cntxt.startActivity(intent);
        }

        return token;
    }

    public static void setToken(String token) {
        editor.putString( nameKey, token );
        editor.commit();
        YandexApi.token = token;
    }

    public static String getOauthUrl() {
        return "https://oauth.yandex.ru/authorize?response_type=token&client_id="+apiId+"&redirect_uri="+url+"://token";
    }


}

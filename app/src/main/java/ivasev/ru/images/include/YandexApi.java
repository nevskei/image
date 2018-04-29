package ivasev.ru.images.include;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.yandex.disk.rest.Credentials;
import com.yandex.disk.rest.OkHttpClientFactory;
import com.yandex.disk.rest.ResourcesArgs;
import com.yandex.disk.rest.RestClient;
import com.yandex.disk.rest.exceptions.ServerIOException;
import com.yandex.disk.rest.json.Resource;
import com.yandex.disk.rest.json.ResourceList;
import com.yandex.disk.rest.retrofit.ErrorHandlerImpl;
import com.yandex.disk.rest.retrofit.RequestInterceptorImpl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ivasev.ru.images.*;
import ivasev.ru.images.include.data.Image;
import retrofit.RestAdapter;
import retrofit.client.OkClient;

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

    public static List<ImageItem> downloadImage(Context context) {

        final List<ImageItem> mYandexImages = new ArrayList<>();

        Credentials credentials = new Credentials("", token);

        CloudApi cloudApi = new RestAdapter.Builder()
            .setClient(new OkClient(OkHttpClientFactory.makeClient()))
            .setEndpoint("https://cloud-api.yandex.net")
            .setRequestInterceptor(new RequestInterceptorImpl(credentials.getHeaders()))
            .setErrorHandler(new ErrorHandlerImpl())
            .build()
            .create(CloudApi.class);
        try {
            RestClient restClient = new RestClient(new Credentials("", token));
            ResourcesArgs.Builder builder = new ResourcesArgs.Builder();
            builder.setMediaType("image");
            ResourcesArgs resourcesArgs = builder.build();
            ResourceList resourceList = restClient.getFlatResourceList(resourcesArgs);
            Image.clearing(context);
            for (Iterator<Resource> i = resourceList.getItems().iterator(); i.hasNext();) {
                Resource item = i.next();
                if (item != null) {

                    builder = new ResourcesArgs.Builder();
                    builder.setPath(item.getPath().toString());
                    resourcesArgs = builder.build();
                    ResourceFile img = cloudApi.getResources(resourcesArgs.getPath(), resourcesArgs.getFields(),
                            resourcesArgs.getLimit(), resourcesArgs.getOffset(), resourcesArgs.getSort(), resourcesArgs.getPreviewSize(),
                            resourcesArgs.getPreviewCrop());
                    new Image(context, img.getName(), img.getFile(), "").save();
                    mYandexImages.add(new ImageItem(img.getFile(), img.getName()));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ServerIOException e) {
            e.printStackTrace();
        }
        return mYandexImages;
    }
}

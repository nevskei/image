package ivasev.ru.images;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import ivasev.ru.images.include.data.Image;

public class LoadService extends IntentService {
    private static final String ACTION_FOO = "ivasev.ru.images.action.load";

    public LoadService() {
        super("LoadService");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            try {
                handleActionList();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleActionList() throws MalformedURLException {
        List<Image> images = Image.getList(this, null, null, null);

        for (Iterator<Image> i = images.iterator(); i.hasNext();) {
            Image image = i.next();
            Boolean isLoad = false;
            if (image.local_path == null) {
                isLoad = true;
            } else {
                File f = new File(image.local_path);
                if (!f.exists()) {
                    isLoad = true;
                }
            }
            if (isLoad && image.url != null && image.name != null) {
                File file = new File(this.getFilesDir(), image.name);
                try {
                    FileUtils.copyURLToFile(new URL(image.url), file);
                    image.local_path = this.getFilesDir() +"/"+ image.name;
                    image.save();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }

    }

}

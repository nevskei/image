package ivasev.ru.images;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import ivasev.ru.images.include.*;

public class DetailActivity extends AppCompatActivity {

    public static final String EXTRA_PHOTO = "PhotoActivity";
    private ImageView mImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_detail);

        mImageView = findViewById(R.id.image);
        ImageItem image = getIntent().getParcelableExtra(EXTRA_PHOTO);
        Glide.with(this)
                .load(image.getUrl())
                .asBitmap()
                .error(R.drawable.ic_launcher_foreground)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(mImageView);
    }
}

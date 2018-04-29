package ivasev.ru.images;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import ivasev.ru.images.include.*;
import ivasev.ru.images.include.data.Image;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        String token = "";

        // check if this intent is started via custom scheme link


        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            Uri uri = intent.getData();
            uri = Uri.parse(uri.toString().replace('#', '?'));
            try{
                token =  uri.getQueryParameter("access_token");
                YandexApi.setToken(token);
            } catch(NullPointerException e) {
            }
        } else {
            token = YandexApi.getToken(this);
        }

        if (token != null) {

            final List<ImageItem> mImages = new ArrayList<>();
            TextView textView = findViewById(R.id.textView);
            textView.setText(R.string.images_yandex);
            layoutManager = new GridLayoutManager(MainActivity.this, 2);
            recyclerView = findViewById(R.id.rv_images);
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(layoutManager);
            final ImageGalleryAdapter adapter = new ImageGalleryAdapter(MainActivity.this, mImages);
            recyclerView.setAdapter(adapter);


            final List<ImageItem> mYandexImages = new ArrayList<>();
            if (Tool.hasConnection(this)) {

                Thread myThready = new Thread(new Runnable() {
                    public void run() {
                        mYandexImages.addAll(YandexApi.downloadImage(MainActivity.this));
                    }
                });
                myThready.start();
                try {
                    myThready.join();
                    adapter.setItems(mYandexImages);
                    Intent intentMyIntentService = new Intent(this, LoadService.class);
                    startService(intentMyIntentService);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                textView.setText(R.string.images_disc);
                List<Image> images = Image.getList(this, null, null, null);

                for (Iterator<Image> i = images.iterator(); i.hasNext();) {
                    Image item = i.next();

                    if (item.local_path == "" || !(new File(item.local_path).exists()))
                        continue;
                    mYandexImages.add(new ImageItem(item.local_path, item.name));
                }

                adapter.setItems(mYandexImages);
            }
        } else {
            intent = new Intent(MainActivity.this, YandexActivity.class);
            startActivity(intent);
        }
    }

    private class ImageGalleryAdapter extends RecyclerView.Adapter<ImageGalleryAdapter.MyViewHolder>  {

        @Override
        public ImageGalleryAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            Context context = parent.getContext();
            LayoutInflater inflater = LayoutInflater.from(context);
            View photoView = inflater.inflate(R.layout.item_photo, parent, false);
            ImageGalleryAdapter.MyViewHolder viewHolder = new ImageGalleryAdapter.MyViewHolder(photoView);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(ImageGalleryAdapter.MyViewHolder holder, int position) {

            ImageItem image = mImages.get(position);
            ImageView imageView = holder.mPhotoImageView;
            Glide.with(mContext)
                .load(image.getUrl())
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .placeholder(R.drawable.ic_launcher_foreground)
                .into(imageView);
        }

        @Override
        public int getItemCount() {
            return (mImages.size());
        }

        public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            public ImageView mPhotoImageView;

            public MyViewHolder(View itemView) {

                super(itemView);
                mPhotoImageView = itemView.findViewById(R.id.iv_photo);
                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View view) {
                int position = getAdapterPosition();
                if(position != RecyclerView.NO_POSITION) {
                    ImageItem image = mImages.get(position);
                    Intent intent = new Intent(mContext, DetailActivity.class);
                    intent.putExtra(DetailActivity.EXTRA_PHOTO, image);
                    startActivity(intent);
                }
            }
        }

        private List<ImageItem> mImages;
        private Context mContext;

        public ImageGalleryAdapter(Context context, List<ImageItem> images) {
            mContext = context;
            mImages = images;
        }

        public void setItems(Collection<ImageItem> images) {
            mImages.addAll(images);
            notifyDataSetChanged();
        }

        public void setItem(ImageItem image) {
            mImages.add(image);
            notifyDataSetChanged();
        }
    }
}


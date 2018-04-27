package ivasev.ru.images;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import ivasev.ru.images.include.CloudApi;
import ivasev.ru.images.include.Image;
import ivasev.ru.images.include.ResourceFile;
import ivasev.ru.images.include.YandexApi;
import retrofit.RestAdapter;
import retrofit.client.OkClient;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private CloudApi cloudApi;
    private Credentials credentials;


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

            final List<Image> mImages = new ArrayList<>();

            layoutManager = new GridLayoutManager(MainActivity.this, 2);
            recyclerView = findViewById(R.id.rv_images);
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(layoutManager);
            final ImageGalleryAdapter adapter = new ImageGalleryAdapter(MainActivity.this, mImages);
            recyclerView.setAdapter(adapter);


            credentials = new Credentials("", token);

            cloudApi = new RestAdapter.Builder()
                    .setClient(new OkClient(OkHttpClientFactory.makeClient()))
                    .setEndpoint("https://cloud-api.yandex.net")
                    .setRequestInterceptor(new RequestInterceptorImpl(credentials.getHeaders()))
                    .setErrorHandler(new ErrorHandlerImpl())
                    .build()
                    .create(CloudApi.class);





            final List<Image> mYandexImages = new ArrayList<>();
            final String finalToken = token;
            Thread myThready = new Thread(new Runnable()
            {
                public void run()
                {
                try {
                    RestClient restClient = new RestClient(new Credentials("", finalToken));
                    ResourcesArgs.Builder builder = new ResourcesArgs.Builder();
                    builder.setMediaType("image");
                    ResourcesArgs resourcesArgs = builder.build();
                    ResourceList resourceList = restClient.getFlatResourceList(resourcesArgs);
                    for (Iterator<Resource> i = resourceList.getItems().iterator(); i.hasNext();) {
                        Resource item = i.next();
                        if (item != null) {

                            builder = new ResourcesArgs.Builder();
                            builder.setPath(item.getPath().toString());
                            resourcesArgs = builder.build();
                            ResourceFile img = cloudApi.getResources(resourcesArgs.getPath(), resourcesArgs.getFields(),
                                    resourcesArgs.getLimit(), resourcesArgs.getOffset(), resourcesArgs.getSort(), resourcesArgs.getPreviewSize(),
                                    resourcesArgs.getPreviewCrop());
                            mYandexImages.add(new Image(img.getFile(), img.getName()));
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ServerIOException e) {
                    e.printStackTrace();
            }
                }
            });
            myThready.start();
            try {
                myThready.join();
                adapter.setItems(mYandexImages);
            } catch (InterruptedException e) {
                e.printStackTrace();
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

            Image image = mImages.get(position);
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
                    Image image = mImages.get(position);
                    Intent intent = new Intent(mContext, DetailActivity.class);
                    intent.putExtra(DetailActivity.EXTRA_PHOTO, image);
                    startActivity(intent);
                }
            }
        }

        private List<Image> mImages;
        private Context mContext;

        public ImageGalleryAdapter(Context context, List<Image> images) {
            mContext = context;
            mImages = images;
        }

        public void setItems(Collection<Image> images) {
            mImages.addAll(images);
            notifyDataSetChanged();
        }

        public void setItem(Image image) {
            mImages.add(image);
            notifyDataSetChanged();
        }
    }
}


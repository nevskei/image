package ivasev.ru.images.include;

import android.os.Parcel;
import android.os.Parcelable;

public class ImageItem implements Parcelable {

    private String file;
    private String name;

    public ImageItem(String url, String title) {
        file = url;
        name = title;
    }

    protected ImageItem(Parcel in) {
        file = in.readString();
        name = in.readString();
    }

    public static final Creator<ImageItem> CREATOR = new Creator<ImageItem>() {
        @Override
        public ImageItem createFromParcel(Parcel in) {
            return new ImageItem(in);
        }

        @Override
        public ImageItem[] newArray(int size) {
            return new ImageItem[size];
        }
    };

    public String getUrl() {
        return file;
    }

    public void setUrl(String url) {
        file = url;
    }

    public String getTitle() {
        return name;
    }

    public void setTitle(String title) {
        name = title;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(file);
        parcel.writeString(name);
    }
}

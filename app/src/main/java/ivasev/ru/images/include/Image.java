package ivasev.ru.images.include;

import android.os.Parcel;
import android.os.Parcelable;

public class Image implements Parcelable {

    private String file;
    private String name;

    public Image(String url, String title) {
        file = url;
        name = title;
    }

    protected Image(Parcel in) {
        file = in.readString();
        name = in.readString();
    }

    public static final Creator<Image> CREATOR = new Creator<Image>() {
        @Override
        public Image createFromParcel(Parcel in) {
            return new Image(in);
        }

        @Override
        public Image[] newArray(int size) {
            return new Image[size];
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

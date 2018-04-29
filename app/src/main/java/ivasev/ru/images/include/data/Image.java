package ivasev.ru.images.include.data;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import java.util.ArrayList;
import java.util.List;

public class Image {

    private static ImagesDbHelper mDbHelper;
    private static SQLiteDatabase db;

    private long id;
    public String name;
    public String url;
    public String local_path;


    private static void init(Context context) {
        mDbHelper = new ImagesDbHelper(context);
        db = mDbHelper.getWritableDatabase();
    }

    public  Image(Context context, int id, String name, String url, String local_path) {
        if (db == null) {
            Image.init(context);
        }
        this.id = id;
        this.name = name;
        this.local_path = local_path;
        this.url = url;
    }


    public Image(Context context, String name, String url, String local_path) {
        if (db == null) {
            Image.init(context);
        }
        this.id = -1;
        this.name = name;
        this.local_path = local_path;
        this.url = url;
    }

    /**
     * Получение списка картинок из базы
     * @param context
     * @param projection
     * @param selection
     * @param selectionArgs
     * @return
     */
    public static List<Image> getList(Context context, String[] projection, String selection, String[] selectionArgs) {
        if (db == null) {
            Image.init(context);
        }
        List<Image> imageList = new ArrayList<>();
        if (projection == null) {
            projection = new String[]{
                ImageEntry._ID,
                ImageEntry.COLUMN_NAME,
                ImageEntry.COLUMN_URL,
                ImageEntry.COLUMN_LOCAL_PATH
            };
        }

        Cursor cursor = db.query(
            ImageEntry.TABLE_NAME,   // таблица
            projection,            // столбцы
            selection,                  // столбцы для условия WHERE
            selectionArgs,                  // значения для условия WHERE
            null,                  // Don't group the rows
            null,                  // Don't filter by row groups
            null);                   // порядок сортировки

        try {

            int idColumnIndex = cursor.getColumnIndex(ImageEntry._ID);
            int nameColumnIndex = cursor.getColumnIndex(ImageEntry.COLUMN_NAME);
            int urlColumnIndex = cursor.getColumnIndex(ImageEntry.COLUMN_URL);
            int loclPathColumnIndex = cursor.getColumnIndex(ImageEntry.COLUMN_LOCAL_PATH);

            // Проходим через все ряды
            while (cursor.moveToNext()) {
                imageList.add(new Image(context,
                        cursor.getInt(idColumnIndex),
                        cursor.getString(nameColumnIndex),
                        cursor.getString(urlColumnIndex),
                        cursor.getString(loclPathColumnIndex)
                ));
            }
        } finally {
            // Всегда закрываем курсор после чтения
            cursor.close();
        }
        return imageList;
    }

    public static void clearing(Context context) {
        if (db == null) {
            Image.init(context);
        }
        db.delete(ImageEntry.TABLE_NAME,  null, null);
    }

    /**
     * Сохранение в базу
     */
    public void save() {
        ContentValues values = new ContentValues();
        values.put(ImageEntry.COLUMN_NAME, name);
        values.put(ImageEntry.COLUMN_URL, url);
        values.put(ImageEntry.COLUMN_LOCAL_PATH, local_path);
        if (id > 0) {
            db.update(ImageEntry.TABLE_NAME, values, ImageEntry._ID + "= ?", new String[]{Long.toString(id)});
        } else {
            db.insert(ImageEntry.TABLE_NAME, null, values);
        }
    }

    /**
     * Удаление из базы
     * если не задан id запрос на удаление не отправляется
     * @return
     */
    public int delete() {
        if (id > 0)
            return db.delete(ImageEntry.TABLE_NAME,  ImageEntry._ID + "= ?", new String[]{Long.toString(id)});
        return -1;
    }



    public static class ImageEntry implements BaseColumns {
        public final static String TABLE_NAME = "images";

        public final static String _ID = BaseColumns._ID;
        public final static String COLUMN_NAME = "name";
        public final static String COLUMN_URL = "url";
        public final static String COLUMN_LOCAL_PATH = "local_path";

    }

    public static class ImagesDbHelper  extends SQLiteOpenHelper {

        public static final String LOG_TAG = ImagesDbHelper.class.getSimpleName();

        private static final String DATABASE_NAME = "images.db";

        private static final int DATABASE_VERSION = 1;

        public ImagesDbHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            // Строка для создания таблицы
            String SQL_CREATE_GUESTS_TABLE = "CREATE TABLE " + ImageEntry.TABLE_NAME + " ("
                    + ImageEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + ImageEntry.COLUMN_NAME + " TEXT NOT NULL, "
                    + ImageEntry.COLUMN_URL + " TEXT NOT NULL, "
                    + ImageEntry.COLUMN_LOCAL_PATH + " TEXT NOT NULL);";

            // Запускаем создание таблицы
            db.execSQL(SQL_CREATE_GUESTS_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }
}


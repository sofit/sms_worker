package sofit.sms_worker;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class SmsWorkerOpenHelper extends SQLiteOpenHelper {

  private static final String TAG = "SmsWorkerOpenHelper";
  private static final String DATABASE_NAME = "sms_worker.sqlite";
  private static final int DATABASE_VERSION = 1;
  public static final String TABLE_NAME = "send_queue";
  public static final String _ID = "_id";
  public static final String RECIPIENT = "recipient";
  public static final String BODY = "body";
  public static final String SEND_DATE = "send_date";

  public SmsWorkerOpenHelper(Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    Log.i(TAG, "creating db...");
    db.execSQL("CREATE TABLE " + TABLE_NAME + " ( " +
        _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
        RECIPIENT + " TEXT, " +
        BODY + " TEXT, " +
        SEND_DATE + " DATETIME);");
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    Log.d(TAG, "upgrade db from " + oldVersion + " to " + newVersion);
    db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
    onCreate(db);
  }
}
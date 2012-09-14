package sofit.sms_worker.activities;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SmsWorkerOpenHelper extends SQLiteOpenHelper {

  private static final String DATABASE_NAME = "sms_worker.sqlite";
  private static final int DATABASE_VERSION = 1;
  public static final String TABLE_NAME = "send_queue";
  public static final String RECIPIENT_COLUMN = "recipient";
  public static final String BODY_COLUMN = "body";
  public static final String SEND_DATE_COLUMN = "send_date";

  private static final String DICTIONARY_TABLE_CREATE =
      "CREATE TABLE " + TABLE_NAME + " (" +
          RECIPIENT_COLUMN + " TEXT, " +
          BODY_COLUMN + " TEXT, " +
          SEND_DATE_COLUMN + " DATETIME);";

  public SmsWorkerOpenHelper(Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    db.execSQL(DICTIONARY_TABLE_CREATE);
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    //TODO: implement this method
  }
}
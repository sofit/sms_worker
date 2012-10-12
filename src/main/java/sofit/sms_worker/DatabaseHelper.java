package sofit.sms_worker;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

  private static final String TAG = "DatabaseHelper";
  private static final String DATABASE_NAME = "sms_worker.sqlite";
  private static final int DATABASE_VERSION = 1;
  public static final String TABLE_NAME = "send_queue";
  public static final String _ID = "_id";
  public static final String RECIPIENT = "recipient";
  public static final String BODY = "body";
  public static final String SEND_DATETIME = "send_datetime";

  public DatabaseHelper(Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    Log.i(TAG, "creating db...");
    db.execSQL("CREATE TABLE " + TABLE_NAME + " ( " +
        _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
        RECIPIENT + " TEXT, " +
        BODY + " TEXT, " +
        SEND_DATETIME + " DATETIME);");
  }


  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    Log.d(TAG, "upgrade db from " + oldVersion + " to " + newVersion);
    db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
    onCreate(db);
  }

  public void addQueueElement(QueueElement queueElement) {
    SQLiteDatabase db = getWritableDatabase();
    ContentValues contentValues = new ContentValues();
    contentValues.put(BODY, queueElement.getBody());
    contentValues.put(RECIPIENT, queueElement.getRecipient());
    contentValues.put(SEND_DATETIME, queueElement.getSendDatetime());
    queueElement.setId(db.insert(TABLE_NAME, null, contentValues));
  }

  public Cursor getQueueElementCursor() {
    SQLiteDatabase db = getReadableDatabase();

    return db.query(TABLE_NAME, new String[] {_ID, SEND_DATETIME, RECIPIENT, BODY},
        null, null, null, null, DatabaseHelper.SEND_DATETIME);
  }

  public List<QueueElement> getQueueElements(String selection, String[] selectionArgs) {
    SQLiteDatabase db = getReadableDatabase();

    List<QueueElement> queueElementList = new ArrayList<QueueElement>();

    Cursor cursor = db.query(TABLE_NAME, new String[] {_ID, SEND_DATETIME, RECIPIENT, BODY},
        selection, selectionArgs, null, null, DatabaseHelper.SEND_DATETIME);

    // looping through all rows and adding to list
    if (cursor.moveToFirst()) {
      do {
        QueueElement queueElement = new QueueElement();
        queueElement.setId(cursor.getLong(0));
        queueElement.setSendDatetime(cursor.getString(1));
        queueElement.setRecipient(cursor.getString(2));
        queueElement.setBody(cursor.getString(3));
        // Adding contact to list
        queueElementList.add(queueElement);
      }
      while (cursor.moveToNext());
    }

    // return contact list
    return queueElementList;
  }

  public List<QueueElement> getAllQueueElements() {
    return getQueueElements(null, null);
  }

  public void delete(QueueElement queueElement) {
    SQLiteDatabase db = getWritableDatabase();
    db.delete(TABLE_NAME, _ID + " = ?", new String[] {String.valueOf(queueElement.getId())});
  }

  public long getQueueElementsCount() {
    SQLiteDatabase db = getReadableDatabase();
    SQLiteStatement statement = db.compileStatement("SELECT COUNT(_id) FROM " + TABLE_NAME);
    return statement.simpleQueryForLong();
  }
}
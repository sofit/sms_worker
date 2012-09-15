package sofit.sms_worker;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

/**
 * @author evgeny
 */
public class QueueActivity extends Activity {

  private final SmsWorkerOpenHelper smsWorkerOpenHelper = new SmsWorkerOpenHelper(this);

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.inbox);

    SQLiteDatabase sqLiteDatabase = smsWorkerOpenHelper.getReadableDatabase();
    Cursor cursor = sqLiteDatabase.query(SmsWorkerOpenHelper.TABLE_NAME, new String[] {
        SmsWorkerOpenHelper._ID, SmsWorkerOpenHelper.SEND_DATE, SmsWorkerOpenHelper.RECIPIENT,
        SmsWorkerOpenHelper.BODY}, null, null, null, null, SmsWorkerOpenHelper.SEND_DATE);

    // the desired columns to be bound
    String[] columns = new String[] {SmsWorkerOpenHelper.SEND_DATE, SmsWorkerOpenHelper.RECIPIENT, SmsWorkerOpenHelper.BODY};
    // the XML defined views which the data will be bound to
    int[] to = new int[] {R.id.entry_date, R.id.entry_address, /*R.id.person_entry, */R.id.entry_body};

    SimpleCursorAdapter listAdapter = new SimpleCursorAdapter(this, R.layout.sms_list_entry, cursor, columns, to);

    ListView listView = (ListView) findViewById(R.id.inbox_list);
    listView.setAdapter(listAdapter);
    /*listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

      @Override
      public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Cursor data = (Cursor) adapterView.getItemAtPosition(i);
        String address = data.getString(3);
        String body = data.getString(4);
        TextView addressView = (TextView) findViewById(R.id.address);
        addressView.setText(address);
        TextView bodyView = (TextView) findViewById(R.id.body);
        bodyView.setText(body);
      }
    });*/
  }
}
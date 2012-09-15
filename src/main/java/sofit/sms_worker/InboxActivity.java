package sofit.sms_worker;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

/**
 * @author evgeny
 */
public class InboxActivity extends Activity {

  public static final Uri SMS_DRAFT_CONTENT_URI = Uri.parse("content://sms/inbox");

  private Cursor cursor;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.inbox);

    cursor = getContentResolver().query(
        SMS_DRAFT_CONTENT_URI,
        new String[] {"_id", "date", "person", "address", "body"},
        null,
        null,
        "date DESC");

    // the desired columns to be bound
    String[] columns = new String[] {"date", /*"person", */"address", "body"};
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

  @Override
  protected void onDestroy() {
    super.onDestroy();

    if (cursor != null)
      cursor.close();
  }
}

package sofit.sms_worker.activities;

import java.util.Arrays;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import sofit.sms_worker.R;

public class MainActivity extends Activity {

  private static final String TAG = "MainActivity";
  public static final Uri SMS_DRAFT_CONTENT_URI = Uri.parse("content://sms/draft");

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
    Cursor cursor = getContentResolver().query(
        SMS_DRAFT_CONTENT_URI,
        new String[] {"_id", "date", "body"},
        null,
        null,
        "date DESC");
    Log.d(TAG, "cursor = " + cursor);
    Log.d(TAG, "cursor.getColumnNames() = " + Arrays.toString(cursor.getColumnNames()));
    startManagingCursor(cursor);
    // the desired columns to be bound
    String[] columns = new String[] {"date", "body"};
    // the XML defined views which the data will be bound to
    int[] to = new int[] {R.id.date_entry, R.id.body_entry};
    SimpleCursorAdapter listAdapter = new SimpleCursorAdapter(this, R.layout.sms_list_entry, cursor, columns, to);
    ListView listView = (ListView) findViewById(R.id.listView);
    listView.setAdapter(listAdapter);
    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

      @Override
      public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Cursor data = (Cursor) adapterView.getItemAtPosition(i);
        String date = data.getString(1);
        String body = data.getString(2);
        TextView dateView = (TextView) findViewById(R.id.date);
        dateView.setText(date);
        TextView bodyView = (TextView) findViewById(R.id.body);
        bodyView.setText(body);
        showSmsForm(view);
      }
    });
  }

  public void showSmsForm(View view) {
    LinearLayout linearLayout = (LinearLayout) findViewById(R.id.editView);
    linearLayout.setVisibility(LinearLayout.VISIBLE);
  }

  public void scheduleSms(View view) {

  }
}
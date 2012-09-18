package sofit.sms_worker;

import android.app.Activity;
import android.app.ListFragment;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;

/**
 * @author evgeny
 */
public class QueueActivity extends Activity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
//    setContentView(R.layout.queue);

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

  public static class QueueFragment extends ListFragment {

    private Cursor cursor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      DatabaseHelper dbHelper = ((MainApplication) getActivity().getApplication()).getDbHelper();
      cursor = dbHelper.getQueueElementCursor();

      // the desired columns to be bound
      String[] columns = new String[] {DatabaseHelper.SEND_DATETIME, DatabaseHelper.RECIPIENT, DatabaseHelper.BODY};
      // the XML defined views which the data will be bound to
      int[] to = new int[] {R.id.entry_date, R.id.entry_address, /*R.id.person_entry, */R.id.entry_body};

      SimpleCursorAdapter listAdapter = new SimpleCursorAdapter(getActivity(), R.layout.sms_list_entry, cursor, columns, to);
      setListAdapter(listAdapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
      return inflater.inflate(R.layout.queue, container, false);
    }

    @Override
    public void onDestroy() {
      super.onDestroy();

      if (cursor != null)
        cursor.close();
    }
  }
}

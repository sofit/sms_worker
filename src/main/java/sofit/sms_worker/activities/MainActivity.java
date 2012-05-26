package sofit.sms_worker.activities;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import sofit.sms_worker.R;

public class MainActivity extends Activity {

  private static final String TAG = "MainActivity";
  public static final Uri SMS_DRAFT_CONTENT_URI = Uri.parse("content://sms/inbox");

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
    Cursor cursor = getContentResolver().query(
        SMS_DRAFT_CONTENT_URI,
        new String[] {"_id", "date", "person", "address", "body"},
        null,
        null,
        "date DESC");

    Cursor peopleCursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, PEOPLE_PROJECTION, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
    ContactListAdapter contactAdapter = new ContactListAdapter(this, peopleCursor);
    MultiAutoCompleteTextView addressView = (MultiAutoCompleteTextView) findViewById(R.id.address);
    addressView.setAdapter(contactAdapter);
    addressView.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());

    startManagingCursor(cursor);
    // the desired columns to be bound
    String[] columns = new String[] {"date", /*"person", */"address", "body"};
    // the XML defined views which the data will be bound to
    int[] to = new int[] {R.id.date_entry, R.id.address_entry, /*R.id.person_entry, */R.id.body_entry};
    SimpleCursorAdapter listAdapter = new SimpleCursorAdapter(this, R.layout.sms_list_entry, cursor, columns, to);
    ListView listView = (ListView) findViewById(R.id.listView);
    listView.setAdapter(listAdapter);
    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

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
    });
  }

  public void sendSms(View view) {
    TextView addressView = (TextView) findViewById(R.id.address);
    TextView bodyView = (TextView) findViewById(R.id.body);
    SmsManager smsManager = SmsManager.getDefault();
    smsManager.sendTextMessage(addressView.getText().toString(), null, bodyView.getText().toString(), null, null);
    addressView.setText("");
    bodyView.setText("");
  }

  public static class ContactListAdapter extends CursorAdapter implements Filterable {

    private ContentResolver mContent;

    public ContactListAdapter(Context context, Cursor c) {
      super(context, c);
      mContent = context.getContentResolver();
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
      ((TextView) view).setText(cursor.getString(1));
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
      final LayoutInflater inflater = LayoutInflater.from(context);
      final TextView view = (TextView) inflater.inflate(
          android.R.layout.simple_dropdown_item_1line, parent, false);
      view.setText(cursor.getString(1));
      return view;
    }

    @Override
    public String convertToString(Cursor cursor) {
      return cursor.getString(2);
    }

    @Override
    public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
      if (getFilterQueryProvider() != null) {
        return getFilterQueryProvider().runQuery(constraint);
      }

      StringBuilder buffer = null;
      String[] args = null;
      if (constraint != null) {
        buffer = new StringBuilder();
//        buffer.append("UPPER("); TODO: upper with unicode does not work
        buffer.append(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
//        buffer.append(") LIKE ?");
        buffer.append(" LIKE ?");
        args = new String[] {"%" + constraint.toString() + "%"};
      }

      return mContent.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, PEOPLE_PROJECTION,
          buffer == null ? null : buffer.toString(), args,
          ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
    }
  }

  private static final String[] PEOPLE_PROJECTION = new String[] {
      ContactsContract.CommonDataKinds.Phone._ID,
      ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
      ContactsContract.CommonDataKinds.Phone.NUMBER
  };
}
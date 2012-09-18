package sofit.sms_worker;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;

/**
 * @author evgeny
 */
public class SendDatePickerDialog extends DialogFragment {

  private DatePickerDialog.OnDateSetListener onDateSetListener;
  private final int year;
  private final int month;
  private final int day;

  public SendDatePickerDialog(DatePickerDialog.OnDateSetListener onDateSetListener, int year, int month, int day) {
    this.onDateSetListener = onDateSetListener;
    this.year = year;
    this.month = month;
    this.day = day;
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    // Create a new instance of DatePickerDialog and return it
    return new DatePickerDialog(getActivity(), onDateSetListener, year, month, day);
  }
}

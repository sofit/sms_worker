package sofit.sms_worker;

import java.util.Calendar;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;

import static android.app.TimePickerDialog.OnTimeSetListener;

/**
 * @author evgeny
 */
public class SendTimePickerDialog extends DialogFragment {

  private OnTimeSetListener onTimeSetListener;
  private final int hour;
  private final int minute;

  public SendTimePickerDialog(OnTimeSetListener onTimeSetListener, int hour, int minute) {
    this.onTimeSetListener = onTimeSetListener;
    this.hour = hour;
    this.minute = minute;
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    // Create a new instance of TimePickerDialog and return it
    return new TimePickerDialog(getActivity(), onTimeSetListener, hour, minute,
        DateFormat.is24HourFormat(getActivity()));
  }
}

package org.bosik.diacomp.android.face.activities;

import java.util.Date;
import org.bosik.diacomp.android.face.R;
import org.bosik.diacomp.android.face.UIUtils;
import org.bosik.diacomp.core.entities.business.diary.records.NoteRecord;
import org.bosik.diacomp.core.entities.tech.Versioned;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;

public class ActivityEditorNote extends ActivityEditor<Versioned<NoteRecord>>
{
	/* =========================== КОНСТАНТЫ ================================ */
	// private static final String TAG = "ActivityEditorNote";

	/* =========================== ПОЛЯ ================================ */

	// компоненты
	private TimePicker	timePicker;
	private DatePicker	datePicker;
	private EditText	editText;
	private Button		buttonOK;

	/* =========================== МЕТОДЫ ================================ */

	@Override
	protected void setupInterface()
	{
		setContentView(R.layout.editor_note);
		timePicker = (TimePicker) findViewById(R.id.pickerNoteTime);
		timePicker.setIs24HourView(true);
		datePicker = (DatePicker) findViewById(R.id.pickerNoteDate);
		editText = (EditText) findViewById(R.id.editNoteText);
		buttonOK = (Button) findViewById(R.id.buttonNoteOK);
		buttonOK.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				ActivityEditorNote.this.submit();
			}
		});
		timePicker.setIs24HourView(true);
	}

	@Override
	protected void showValuesInGUI(boolean createMode)
	{
		if (!createMode)
		{
			showTime(entity.getData().getTime(), datePicker, timePicker);
			editText.setText(entity.getData().getText());
		}
		else
		{
			showTime(new Date(), datePicker, timePicker);
			editText.setText("");
		}
	}

	@Override
	protected boolean getValuesFromGUI()
	{
		// читаем время
		try
		{
			entity.getData().setTime(readTime(datePicker, timePicker));
		}
		catch (IllegalArgumentException e)
		{
			UIUtils.showTip(ActivityEditorNote.this, "Ошибка: неверное время");
			timePicker.requestFocus();
			return false;
		}

		// читаем значение
		try
		{
			entity.getData().setText(editText.getText().toString());
		}
		catch (IllegalArgumentException e)
		{
			UIUtils.showTip(ActivityEditorNote.this, "Ошибка: неверный текст");
			editText.requestFocus();
			return false;
		}

		return true;
	}
}

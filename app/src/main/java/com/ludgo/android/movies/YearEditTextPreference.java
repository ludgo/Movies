package com.ludgo.android.movies;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.EditText;


/**
 * EditTextPreference customised to type a year A.D. in,
 * with possible choice of the number of digits
 */
public class YearEditTextPreference extends EditTextPreference {
    private static final int YEAR_MIN_LENGTH_DEFAULT = 1;
    private static final int YEAR_MAX_LENGTH_DEFAULT = 4;
    private static int yearMinLength;
    private static int yearMaxLength;

    public YearEditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray container = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.YearEditTextPreference,
                0,
                0);
        try {
            yearMinLength = container.getInteger(R.styleable.YearEditTextPreference_minLength,
                    YEAR_MIN_LENGTH_DEFAULT);
            yearMaxLength = container.getInteger(R.styleable.YearEditTextPreference_maxLength,
                    YEAR_MAX_LENGTH_DEFAULT);
        } finally {
            container.recycle();
        }
    }

    @Override
    protected void showDialog(Bundle state) {
        super.showDialog(state);

        EditText editText = getEditText();
        editText.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                Dialog dialog = getDialog();
                if (dialog instanceof AlertDialog) {
                    AlertDialog alertDialog = (AlertDialog) dialog;
                    Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                    // Check if the text matches criteria
                    if (yearMinLength <= s.length() && s.length() <= yearMaxLength) {
                        // Enable OK button
                        positiveButton.setEnabled(true);
                    } else {
                        // Disable OK button.
                        positiveButton.setEnabled(false);
                    }
                }
            }
        });
    }
}
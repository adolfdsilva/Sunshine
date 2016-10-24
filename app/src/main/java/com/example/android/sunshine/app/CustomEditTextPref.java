package com.example.android.sunshine.app;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.support.annotation.RequiresApi;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by Audi on 23/10/16.
 */

public class CustomEditTextPref extends EditTextPreference implements TextWatcher {

    int minLength;
    EditText editText;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public CustomEditTextPref(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        TypedArray a = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.CustomEditTextPref, 0, 0);
        minLength = a.getInt(R.styleable.CustomEditTextPref_minLength, 2);
        a.recycle();
    }

    public CustomEditTextPref(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.CustomEditTextPref, 0, 0);
        minLength = a.getInt(R.styleable.CustomEditTextPref_minLength, 2);
        a.recycle();
    }

    public CustomEditTextPref(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.CustomEditTextPref, 0, 0);
        minLength = a.getInt(R.styleable.CustomEditTextPref_minLength, 2);
        a.recycle();
    }

    public CustomEditTextPref(Context context) {
        super(context);
    }

    @Override
    protected void showDialog(Bundle state) {
        super.showDialog(state);
        editText = getEditText();
        editText.addTextChangedListener(this);
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void afterTextChanged(Editable editable) {Dialog dialog = getDialog();
        if (dialog instanceof AlertDialog) {
            AlertDialog alertDialog = (AlertDialog) dialog;
            Button button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);

            if (editable.length() >= minLength)
                button.setEnabled(true);
            else
                button.setEnabled(false);

        }
    }
}

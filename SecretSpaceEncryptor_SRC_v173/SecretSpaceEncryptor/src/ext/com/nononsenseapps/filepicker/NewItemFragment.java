/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package ext.com.nononsenseapps.filepicker;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.paranoiaworks.unicus.android.sse.R;
import com.paranoiaworks.unicus.android.sse.utils.Helpers;

public abstract class NewItemFragment extends DialogFragment {

    private String itemName = null;
    private View okButton = null;
    private OnNewFolderListener listener = null;

    public NewItemFragment() {
        super();
    }

    public void setListener(final OnNewFolderListener listener) {
        this.listener = listener;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        getDialog().setTitle(getDialogTitle());

        @SuppressLint("InflateParams") final View view =
                inflater.inflate(R.layout.lext_nnfp_dialog_new_item, null);

        okButton = view.findViewById(R.id.button_ok);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (listener != null) {
                    listener.onNewFolder(itemName);
                }
                dismiss();
            }
        });

        view.findViewById(R.id.button_cancel)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        dismiss();
                    }
                });

        final EditText editText = (EditText) view.findViewById(R.id.edit_text);
        editText.setFilters(new InputFilter[] { Helpers.getFileNameInputFilter() });
        if (itemName == null) {
            okButton.setEnabled(false);
        } else {
            editText.setText(itemName);
            validateItemName();
        }

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(final CharSequence s, final int start,
                    final int count, final int after) {
            }

            @Override
            public void onTextChanged(final CharSequence s, final int start,
                    final int before, final int count) {
            }

            @Override
            public void afterTextChanged(final Editable s) {
                itemName = s.toString();
                validateItemName();
            }
        });

        return view;
    }

    protected abstract int getDialogTitle();

    private void validateItemName() {
        if (okButton != null) {
            okButton.setEnabled(validateName(itemName));
        }
    }

    protected abstract boolean validateName(final String itemName);

    public interface OnNewFolderListener {
        /**
         * Name is validated to be non-null, non-empty and not containing any
         * slashes.
         *
         * @param name The name of the folder the user wishes to create.
         */
        public void onNewFolder(final String name);
    }
}

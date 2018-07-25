/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package ext.com.nononsenseapps.filepicker;


import android.app.FragmentManager;

import com.paranoiaworks.unicus.android.sse.R;

public class NewFolderFragment extends NewItemFragment {

    private static final String TAG = "new_folder_fragment";
    public static void showDialog(final FragmentManager fm, final OnNewFolderListener listener) {
        NewItemFragment d = new NewFolderFragment();
        d.setListener(listener);
        d.show(fm, TAG);
    }

    @Override
    protected boolean validateName(final String itemName) {
        return itemName != null && !itemName.isEmpty()
                && !itemName.contains("/");
    }

    @Override
    protected int getDialogTitle() {
        return R.string.nnfp_new_folder;
    }
}

/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package ext.com.nononsenseapps.filepicker;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import android.content.AsyncTaskLoader;
import android.content.Loader;
import android.net.Uri;
import android.os.Environment;
import android.os.FileObserver;
import android.widget.Toast;

import com.paranoiaworks.unicus.android.sse.R;
import com.paranoiaworks.unicus.android.sse.utils.Helpers;

public class FilePickerFragment extends AbstractFilePickerFragment<File> {

    public FilePickerFragment() {}

    /**
     * Return true if the path is a directory and not a file.
     *
     * @param path
     */
    @Override
    protected boolean isDir(final File path) {
        return path.isDirectory();
    }

    /**
     * Return the path to the parent directory. Should return the root if
     * from is root.
     *
     * @param from
     */
    @Override
    protected File getParent(final File from) {
        if (from.getParentFile() != null) {
            if (from.isFile()) {
                return getParent(from.getParentFile());
            } else {
                return from.getParentFile();
            }
        } else {
            return from;
        }
    }

    /**
     * Convert the path to the type used.
     *
     * @param path
     */
    @Override
    protected File getPath(final String path) {
        return new File(path);
    }

    /**
     * @param path
     * @return the full path to the file
     */
    @Override
    protected String getFullPath(final File path) {
        return path.getPath();
    }

    /**
     * @param path
     * @return the name of this file/folder
     */
    @Override
    protected String getName(final File path) {
        return path.getName();
    }

    /**
     * Get the root path (lowest allowed).
     */
    @Override
    protected File getRoot() {
        return Environment.getExternalStorageDirectory();
    }

    /**
     * Convert the path to a URI for the return intent
     * @param file
     * @return
     */
    @Override
    protected Uri toUri(final File file) {
        return Uri.fromFile(file);
    }

    /**
     * @return a comparator that can sort the items alphabetically
     */
    @Override
    protected Comparator<File> getComparator() {
        return new Comparator<File>() {
            @Override
            public int compare(final File lhs, final File rhs) {
                if (lhs.isDirectory() && !rhs.isDirectory()) {
                    return -1;
                } else if (rhs.isDirectory() && !lhs.isDirectory()) {
                    return 1;
                } else {
                    return lhs.getName().toLowerCase().compareTo(rhs.getName()
                            .toLowerCase());
                }
            }
        };
    }

    /**
     * Get a loader that lists the Files in the current path,
     * and monitors changes.
     */
    @Override
    protected Loader<List<File>> getLoader() {
        return new AsyncTaskLoader<List<File>>(getActivity()) {

            FileObserver fileObserver;

            @Override
            public List<File> loadInBackground() {
                ArrayList<File> files = new ArrayList<File>();
                File[] listFiles = currentPath.listFiles();
                if(listFiles != null) {
                    for (java.io.File f : listFiles) {
                        if ((mode == MODE_FILE || mode == MODE_FILE_AND_DIR)
                                || f.isDirectory()) {                           
                        	if(extensionsFilter != null && extensionsFilter.indexOf(getFileExt(f)) < 0 && !f.isDirectory()) continue;
                        	files.add(f);
                        }
                    }
                }
                return files;
            }

            /**
             * Handles a request to start the Loader.
             */
            @Override
            protected void onStartLoading() {
                super.onStartLoading();

                // handle if directory does not exist. Fall back to root.
                if (currentPath == null || !currentPath.isDirectory()) {
                    currentPath = getRoot();
                }

                // Start watching for changes
                fileObserver = new FileObserver(currentPath.getPath(),
                        FileObserver.CREATE |
                                FileObserver.DELETE
                                | FileObserver.MOVED_FROM | FileObserver.MOVED_TO
                ) {

                    @Override
                    public void onEvent(int event, String path) {
                        // Reload
                        onContentChanged();
                    }
                };
                fileObserver.startWatching();

                forceLoad();
            }

            /**
             * Handles a request to completely reset the Loader.
             */
            @Override
            protected void onReset() {
                super.onReset();

                // Stop watching
                if (fileObserver != null) {
                    fileObserver.stopWatching();
                    fileObserver = null;
                }
            }
        };
    }

    /**
     * Name is validated to be non-null, non-empty and not containing any
     * slashes.
     *
     * @param name The name of the folder the user wishes to create.
     */
    @Override
    public void onNewFolder(final String name) {
        File folder = new File(currentPath, name);

        if (folder.mkdir()) {
            currentPath = folder;
            refresh();
        } else {
            Toast.makeText(getActivity(), R.string.nnfp_create_folder_error,
                    Toast.LENGTH_SHORT).show();
        }
    }
    
	private static String getFileExt(File file)
	{
		if(file == null) return null;
		return file.getName().substring(file.getName().lastIndexOf(".") + 1);
	}
}

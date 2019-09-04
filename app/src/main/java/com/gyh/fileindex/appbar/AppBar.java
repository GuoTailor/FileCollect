package com.gyh.fileindex.appbar;

import androidx.annotation.StringRes;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.appbar.AppBarLayout;
import com.gyh.fileindex.MainActivity;
import com.gyh.fileindex.R;

import static android.os.Build.VERSION.SDK_INT;

/**
 * layout_appbar.xml contains the layout for AppBar and BottomBar
 *
 * This is a class containing containing methods to each section of the AppBar,
 * creating the object loads the views.
 *
 * @author Emmanuel
 *         on 2/8/2017, at 23:27.
 */

public class AppBar {

    private int TOOLBAR_START_INSET;

    private Toolbar toolbar;
    private SearchView searchView;

    private AppBarLayout appbarLayout;

    public AppBar(MainActivity a, SearchView.SearchListener searchListener) {
        toolbar = a.findViewById(R.id.action_bar);
        searchView = new SearchView(this, a, searchListener);

        appbarLayout = a.findViewById(R.id.lin);

        if (SDK_INT >= 21) toolbar.setElevation(0);
        /* For SearchView, see onCreateOptionsMenu(Menu menu)*/
        TOOLBAR_START_INSET = toolbar.getContentInsetStart();

    }

    public Toolbar getToolbar() {
        return toolbar;
    }

    public SearchView getSearchView() {
        return searchView;
    }

    public AppBarLayout getAppbarLayout() {
        return appbarLayout;
    }

    public void setTitle(String title) {
        if (toolbar != null) toolbar.setTitle(title);
    }

    public void setTitle(@StringRes int title) {
        if (toolbar != null) toolbar.setTitle(title);
    }

}

package com.gyh.fileindex.appbar;

import android.app.Activity;

import androidx.annotation.StringRes;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.appbar.AppBarLayout;
import com.gyh.fileindex.R;

import static android.os.Build.VERSION.SDK_INT;

/**
 * layout_appbar.xml contains the layout for AppBar and BottomBar
 * <p>
 * This is a class containing containing methods to each section of the AppBar,
 * creating the object loads the views.
 *
 * @author Emmanuel
 * on 2/8/2017, at 23:27.
 */

public class AppBar {

    private int TOOLBAR_START_INSET;

    private Toolbar toolbar;
    private SearchView searchView;

    private AppBarLayout appbarLayout;

    public AppBar(Activity activity, SmokeScreen a, SearchView.SearchListener searchListener) {
        toolbar = activity.findViewById(R.id.action_bar);
        searchView = new SearchView(this, activity, a, searchListener);

        appbarLayout = activity.findViewById(R.id.lin);

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

package com.example.amarsaljic.newstospeech;

import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ExpandableListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TabListActivity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    private List<String> providerNames;
    private List<Article> articleList;
    static List<HashMap<String, List<Article>>> listSortedByProviderCategoryArticles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.articleList = this.getAllArticles();
        this.providerNames = this.getAllProviderNames();
        this.listSortedByProviderCategoryArticles = this.getListOfProvidersWithCategoriesAndRelatedArticles();

        setContentView(R.layout.activity_tab_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);

        for (String providerName: providerNames){
            tabLayout.addTab(tabLayout.newTab().setText(providerName));
        }

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));
    }

    private List<String> getAllProviderNames() {
        List<String> providerNames = new ArrayList<>();
        providerNames.add("All");
        for (Article article : this.articleList) {
            if(!providerNames.contains(article.provider_name)){
                providerNames.add(article.provider_name);
            }
        }

        return providerNames;
    }

    private List<Article> getAllArticles() {
        DefaultArticles da = DefaultArticles.getInstance(this);
        return da.articleList;
    }

    private List<HashMap<String, List<Article>>> getListOfProvidersWithCategoriesAndRelatedArticles(){
        List<HashMap<String, List<Article>>> resultList = new ArrayList<>();

        for (int i = 0; i < this.providerNames.size(); i++) {
            resultList.add(new HashMap<String, List<Article>>());
        }

        for (Article article : this.articleList){
            int indexOfProvider = providerNames.indexOf(article.provider_name);
            String categoryOfArticle = article.category_name;

            HashMap<String, List<Article>> providerHashmap = resultList.get(indexOfProvider);
            if(providerHashmap.get(categoryOfArticle) == null){
                providerHashmap.put(categoryOfArticle, new ArrayList<Article>());
            }
            List<Article> currentArticleListOfCategory = providerHashmap.get(categoryOfArticle);
            currentArticleListOfCategory.add(article);
            providerHashmap.put(categoryOfArticle, currentArticleListOfCategory);

            // Also add in all provider!
            HashMap<String, List<Article>> allHashmap = resultList.get(0);
            if(allHashmap.get(categoryOfArticle) == null){
                allHashmap.put(categoryOfArticle, new ArrayList<Article>());
            }
            List<Article> currentAllArticleListOfCategory = allHashmap.get(categoryOfArticle);
            currentAllArticleListOfCategory.add(article);
            allHashmap.put(categoryOfArticle, currentAllArticleListOfCategory);
        }

        return resultList;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_tab_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static int provider_id;

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            provider_id = sectionNumber;
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_tab_list, container, false);
            ExpandableListView expandableListView = (ExpandableListView) rootView.findViewById(R.id.article_list_view);
            HashMap<String, List<Article>> relevantArticlesSortedInCategories = getRelevantArticlesSortedInCategories(provider_id);
            List<String> availableCategories = getAvailableCategories(provider_id);
            ArticleExpandableListAdapter expandableListAdapter = new ArticleExpandableListAdapter(getContext(), availableCategories, relevantArticlesSortedInCategories);
            expandableListView.setAdapter(expandableListAdapter);
            expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
                @Override
                public boolean onChildClick(ExpandableListView expandableListView, View view, int i, int i1, long l) {
                    // TODO: Open DetailView!
                    return false;
                }
            });
            return rootView;
        }

        private HashMap<String, List<Article>> getRelevantArticlesSortedInCategories(int argSectionNumber) {
            return TabListActivity.listSortedByProviderCategoryArticles.get(argSectionNumber);
        }

        private List<String> getAvailableCategories(int argSectionNumber) {
            return new ArrayList<>(TabListActivity.listSortedByProviderCategoryArticles.get(argSectionNumber).keySet());
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position);
        }

        @Override
        public int getCount() {
            return providerNames.size();
        }
    }
}

package com.example.amarsaljic.newstospeech;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link TabListFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link TabListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TabListFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match

    private TabListFragment.SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    FragmentActivity myContext;

    private List<String> providerNames;
    private List<Article> articleList;
    static List<HashMap<String, List<Article>>> listSortedByProviderCategoryArticles;
    static TabLayout tabLayout;




    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public TabListFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TabListFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TabListFragment newInstance(String param1, String param2) {
        TabListFragment fragment = new TabListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_parent_tablist, container, false);

        this.articleList = this.getAllArticles();
        this.providerNames = this.getAllProviderNames();
        this.listSortedByProviderCategoryArticles = this.getListOfProvidersWithCategoriesAndRelatedArticles();

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getChildFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) v.findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        mViewPager.setCurrentItem(0);

        tabLayout = (TabLayout) v.findViewById(R.id.tabs);

        for (String providerName: providerNames){
            tabLayout.addTab(tabLayout.newTab().setText(providerName));
        }

        Log.i("Tab Count: ", Integer.toString(tabLayout.getTabCount()));

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

        return v;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        myContext = (FragmentActivity) context;
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
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
        DefaultArticles da = DefaultArticles.getInstance(this.getActivity());
        return da.articleList;
    }

    private List<HashMap<String, List<Article>>> initializeList ( int n ){
        ArrayList<HashMap<String, List<Article>>> resultList = new ArrayList();
        for (int i = 0; i < n; i++) {
            resultList.add(new HashMap<String, List<Article>>());
        }
        return resultList;
    }

    private List<HashMap<String, List<Article>>> getListOfProvidersWithCategoriesAndRelatedArticles(){
        List<HashMap<String, List<Article>>> resultList = new ArrayList<>();

        resultList = initializeList( this.providerNames.size() );

        for (Article article : this.articleList){

            int indexOfProvider = providerNames.indexOf(article.provider_name);
            String categoryOfArticle = article.category_name;

            categoryOfArticle = categoryOfArticle.substring(0, 1).toUpperCase() + categoryOfArticle.substring(1);

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

    public class SectionsPagerAdapter extends FragmentStatePagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            Log.i("lol: ", ""+position);
            return TabListFragment.PlaceholderFragment.newInstance(position);
        }

        @Override
        public int getCount() {
            return providerNames.size();
        }
    }

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
                    int indexOfClickedArticle = i1;
                    Intent intent = new Intent(getContext(), PlayActivity.class);
                    intent.putExtra("index", 5);
                    startActivity(intent);
                    return true;
                }
            });

            for (int i = 0; i < availableCategories.size(); i++) {
                expandableListView.expandGroup(i);
            }
            return rootView;
        }

        private HashMap<String, List<Article>> getRelevantArticlesSortedInCategories(int argSectionNumber) {
            HashMap<String, List<Article>> categoriesWithArticles = TabListFragment.listSortedByProviderCategoryArticles.get(argSectionNumber);
            return categoriesWithArticles;
        }

        private List<String> getAvailableCategories(int argSectionNumber) {
            List<String> availableCategories = new ArrayList<>(TabListFragment.listSortedByProviderCategoryArticles.get(argSectionNumber).keySet());
            return availableCategories;
        }
    }

}

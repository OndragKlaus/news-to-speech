package com.example.amarsaljic.newstospeech;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.List;

/**
 * Created by amarsaljic on 18.11.17.
 */

public class ArticleExpandableListAdapter extends BaseExpandableListAdapter {

    private Context context;
    private List<String> availableArticleCategories;
    private HashMap<String, List<Article>> articlesPerCategory;

    public ArticleExpandableListAdapter(Context context, List<String> availableArticleCategories,
                                        HashMap<String, List<Article>> articlesPerCategory) {
        this.context = context;
        this.availableArticleCategories = availableArticleCategories;
        this.articlesPerCategory = articlesPerCategory;
    }

    @Override
    public int getGroupCount() {
        return this.availableArticleCategories.size();
    }

    @Override
    public int getChildrenCount(int i) {
        return this.articlesPerCategory.get(this.availableArticleCategories.get(i)).size();
    }

    @Override
    public Object getGroup(int i) {
        return this.availableArticleCategories.get(i);
    }

    @Override
    public Object getChild(int i, int i1) {
        return this.articlesPerCategory.get(this.availableArticleCategories.get(i)).get(i1);
    }

    @Override
    public long getGroupId(int i) {
        return i;
    }

    @Override
    public long getChildId(int i, int i1) {
        return i1;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int i, boolean b, View view, ViewGroup viewGroup) {
        String categoryName = (String) getGroup(i);
        if(view == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            view = layoutInflater.inflate(R.layout.article_list_group, null);
        }
        TextView categoryTextView = (TextView) view.findViewById(R.id.article_list_view_header);
        categoryTextView.setText(categoryName);
        return view;
    }

    @Override
    public View getChildView(int i, int i1, boolean b, View view, ViewGroup viewGroup) {
        Article article = (Article) getChild(i, i1);
        String articleTitle = article.title;
        if(view == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            view = layoutInflater.inflate(R.layout.article_list_item, null);
        }
        TextView articleTextView = (TextView) view.findViewById(R.id.article_list_view_item);
        articleTextView.setText(articleTitle);

        return view;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return true;
    }
}

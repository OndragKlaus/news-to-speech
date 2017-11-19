package com.example.amarsaljic.newstospeech;

import java.util.Date;

/**
 * Created by amarsaljic on 18.11.17.
 */

public class Article {
    String article_id;
    String provider_name;
    String category_name;
    String url;
    String author;
    String title;
    String summary;
    Date date_published;
    Integer audio_file_id;

    public Article(String article_id, String provider_name, String category_name, String url, String author,
                   String title, String summary, Date date_published, Integer audio_file_id) {
        this.article_id = article_id;
        this.provider_name = provider_name;
        this.category_name = category_name;
        this.url = url;
        this.author = author;
        this.title = title;
        this.summary = summary;
        this.date_published = date_published;
        this.audio_file_id = audio_file_id;
    }
}

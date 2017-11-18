package com.example.amarsaljic.newstospeech;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;

import com.opencsv.CSVReader;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by klaus on 18.11.17.
 */

public class DefaultArticles {

    private static DefaultArticles instance;

    public static DefaultArticles getInstance (Context context) {
        if (DefaultArticles.instance == null) {
            DefaultArticles.instance = new DefaultArticles(context);
        }
        return DefaultArticles.instance;
    }

    public List<Article> articleList;
    private static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:.S");

    private DefaultArticles(Context context) {
        articleList = new ArrayList<Article>();
        AssetManager assets = context.getAssets();
        try {
            InputStream csvStream = assets.open("data.csv");
            InputStreamReader csvStreamReader = new InputStreamReader(csvStream);
            CSVReader reader = new CSVReader(csvStreamReader);
            String [] nextLine;
            while ((nextLine = reader.readNext()) != null) {
                String provider;
                if (nextLine[1].equals("de.sueddeutsche")) {
                    provider = "Süddeutsche";
                } else {
                    provider = "Süddeutsche";
                }

                Date publishedDate = new Date();
                try {
                    publishedDate = format.parse(nextLine[6]);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                int mp3File = context.getResources().getIdentifier('a' + nextLine[0],
                        "raw", context.getPackageName());
                Article a = new Article(provider, nextLine[2], nextLine[3], nextLine[4],
                        nextLine[5], nextLine[6], publishedDate, mp3File);
                articleList.add(a);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

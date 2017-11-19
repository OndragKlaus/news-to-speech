package com.example.amarsaljic.newstospeech;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PlayFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link PlayFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PlayFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private Article article;
    private boolean isStared;
    private MediaPlayer article_audio;
    private final Integer skipLength = 10000;
    private final Integer previousReplaysLimit = 5000;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public PlayFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment PlayFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PlayFragment newInstance(String param1, String param2) {
        PlayFragment fragment = new PlayFragment();
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

        View v = inflater.inflate(R.layout.fragment_play, container, false);
        isStared = false;

        DefaultArticles da = DefaultArticles.getInstance(getActivity());
        this.article = da.articleList.get(0);
        article_audio = MediaPlayer.create(getActivity(), this.article.audio_file_id);

        //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
        //          .setAction("Action", null).show();

        final ImageButton replay = (ImageButton) v.findViewById(R.id.replay);
        replay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // or list index == 0

                Integer currentPosition = article_audio.getCurrentPosition();
                if (currentPosition >= skipLength) {
                    article_audio.seekTo(currentPosition - skipLength);
                } else {
                    article_audio.seekTo(0);
                }
            }
        });

        final ImageButton previous = (ImageButton) v.findViewById(R.id.previous);
        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // or list index == 0
                if (article_audio.getCurrentPosition() > previousReplaysLimit) {
                    article_audio.seekTo(0);
                } else {
                    // play previous
                }
            }
        });

        final ImageButton play = (ImageButton) v.findViewById(R.id.play);
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (article_audio.isPlaying()) {
                    article_audio.pause();
                    //Integer l = article_audio.getCurrentPosition();
                    play.setImageResource(R.drawable.ic_play_circle_filled_black_48dp);
                } else {
                    article_audio.start();
                    play.setImageResource(R.drawable.ic_pause_circle_filled_black_48dp);
                }
            }
        });

        final ImageButton next = (ImageButton) v.findViewById(R.id.next);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // if list index == list.length - 1 then stop else next
            }
        });

        final ImageButton star = (ImageButton) v.findViewById(R.id.star);
        star.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isStared) {
                    star.setImageResource(R.drawable.ic_star_black_48dp);
                } else {
                    star.setImageResource(R.drawable.ic_star_border_black_48dp);
                }
                isStared = !isStared;
            }
        });
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
}

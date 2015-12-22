package com.sameer.android.popularmovies;

import android.app.Activity;
import android.support.design.widget.CollapsingToolbarLayout;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.squareup.picasso.Picasso;

/**
 * A fragment representing a single Movie detail screen.
 * This fragment is either contained in a {@link MovieListActivity}
 * in two-pane mode (on tablets) or a {@link MovieDetailActivity}
 * on handsets.
 */
public class MovieDetailFragment extends Fragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_MOVIE = "movie";

    private Movie movie;
    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public MovieDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        if (getArguments().containsKey(ARG_MOVIE)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            movie = getArguments().getParcelable(ARG_MOVIE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.movie_detail, container, false);

        Activity activity = this.getActivity();
        CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
        if (appBarLayout != null) {
            appBarLayout.setTitle(" ");

            ImageView imageView = (ImageView)activity.findViewById(R.id.movie_post);
            String url = Movie.TMDB_BASE_POSTER_URL + movie.getImagePath();
            Picasso.with(getContext())
                    .load(url)
                    .placeholder(R.color.colorPrimary)
                    .into(imageView);
        }

        // Show the dummy content as text in a TextView.
        if (movie != null) {
            ((TextView) rootView.findViewById(R.id.movie_title)).setText(movie.getTitle());
            ((TextView) rootView.findViewById(R.id.movie_details)).setText(movie.getOverview());
            ((TextView) rootView.findViewById(R.id.movie_rating)).setText("Rating: " + movie.getRating());
            ((TextView) rootView.findViewById(R.id.movie_release)).setText("Date: " + movie.getReleaseDate());
        }

        return rootView;
    }
}

package com.sameer.android.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * An activity representing a list of Movies. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link MovieDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class MovieListActivity extends AppCompatActivity {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        View recyclerView = findViewById(R.id.movie_list);
        assert recyclerView != null;

        List<Movie> movies = new ArrayList<>();
        SimpleItemRecyclerViewAdapter viewAdapter = new SimpleItemRecyclerViewAdapter(movies);
        MoviesInfoTask task = new MoviesInfoTask(movies, viewAdapter);
        task.execute((URL) null);

        ((RecyclerView) recyclerView).setAdapter(viewAdapter);

        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this, 2);
        ((RecyclerView) recyclerView).setLayoutManager(layoutManager);

        if (findViewById(R.id.movie_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }
    }

    public class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        private final List<Movie> mValues;

        public SimpleItemRecyclerViewAdapter(List<Movie> items) {
            mValues = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.movie_list_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
//            holder.mIdView.setText(mValues.get(position).getTitle());
//            holder.mContentView.setText(mValues.get(position).getRating());

            int width = getResources().getDisplayMetrics().widthPixels / 2;
            int height = (16 * width) / 9;

            final String TMDB_BASE_URL = "http://image.tmdb.org/t/p/w342";
            String url = TMDB_BASE_URL + mValues.get(position).getImagePath();
            System.out.println(url);

            Picasso.with(getApplicationContext())
                    .load(url)
                    .resize(width, height)
                    .centerCrop()
                    .into(holder.mImageView);

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mTwoPane) {
                        Bundle arguments = new Bundle();
                        arguments.putParcelable(MovieDetailFragment.ARG_MOVIE, mValues.get(position));
                        MovieDetailFragment fragment = new MovieDetailFragment();
                        fragment.setArguments(arguments);
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.movie_detail_container, fragment)
                                .commit();
                    } else {
                        Context context = v.getContext();
                        Intent intent = new Intent(context, MovieDetailActivity.class);
                        intent.putExtra(MovieDetailFragment.ARG_MOVIE, mValues.get(position));

                        context.startActivity(intent);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
//            public final TextView mIdView;
//            public final TextView mContentView;
            public final ImageView mImageView;

            public ViewHolder(View view) {
                super(view);
                mView = view;
//                mIdView = (TextView) view.findViewById(R.id.id);
//                mContentView = (TextView) view.findViewById(R.id.content);
                mImageView = (ImageView) view.findViewById(R.id.image);
            }

            @Override
            public String toString() {
                return super.toString();
//                + " '" + mContentView.getText() + "'";
            }
        }
    }

    private class MoviesInfoTask extends AsyncTask<URL, Integer, Void> {
        private final String LOG_TAG = MoviesInfoTask.class.getSimpleName();
        private List list;
        private SimpleItemRecyclerViewAdapter adapter;

        public MoviesInfoTask(List list, SimpleItemRecyclerViewAdapter adapter) {
            super();
            this.list = list;
            this.adapter = adapter;
        }

        @Override
        protected Void doInBackground(URL... params) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            try {
                final String TMDB_BASE_URL = "http://api.themoviedb.org/3/discover/movie?";
                final String SORT_PARAM = "sort_by";
                final String API_KEY_PARAM = "api_key";
                final String PAGE_PARAM = "units";

                Uri builtUri = Uri.parse(TMDB_BASE_URL).buildUpon()
                        .appendQueryParameter(SORT_PARAM, "popularity.desc")
                        .appendQueryParameter(API_KEY_PARAM, "8dea3a0df3a7f2215def4ff1da625627")
                        .appendQueryParameter(PAGE_PARAM, "1")
                        .build();
                URL url = new URL(builtUri.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                System.out.println(buffer.toString());
                getMoviesInfoFromJson(buffer.toString());
            } catch (Exception e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            ((SimpleItemRecyclerViewAdapter) adapter).notifyDataSetChanged();
        }

        private void getMoviesInfoFromJson(String mJSONStr) throws JSONException {
            final String PAGE = "page";
            final String RESULTS = "results";
            final String TOTAL_PAGES = "total_pages";
            final String TOTAL_RESULTS = "total_results";

            try {
                JSONObject mJSONData = new JSONObject(mJSONStr);
                int pages = mJSONData.getInt(PAGE);
                JSONArray moviesArray = mJSONData.getJSONArray(RESULTS);
                int totalPages = mJSONData.getInt(TOTAL_PAGES);
                int totalResults = mJSONData.getInt(TOTAL_RESULTS);

                for (int index=0; index<moviesArray.length(); index++) {
                    JSONObject jsonObject = moviesArray.getJSONObject(index);
                    String title = jsonObject.getString("original_title");
                    String imagePath = jsonObject.getString("backdrop_path");
                    String overview = jsonObject.getString("overview");
                    String rating = jsonObject.getString("vote_average");
                    String release_date = jsonObject.getString("release_date");

                    Log.i(LOG_TAG, imagePath);
                    Movie movie = new Movie(title, imagePath, overview, rating, release_date);
                    list.add(movie);
                }
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
        }
    }
}

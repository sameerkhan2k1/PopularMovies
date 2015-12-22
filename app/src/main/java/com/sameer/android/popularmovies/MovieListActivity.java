package com.sameer.android.popularmovies;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.squareup.okhttp.Headers;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
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
    private static final String TAG = MovieListActivity.class.getSimpleName();
    private boolean mTwoPane;
    private ArrayList<Movie> mMovieList;
    private SimpleItemRecyclerViewAdapter mAdapter;
    private ProgressBar mProgressBar;
    private enum SortStyle { POP_ASC, POP_DESC, VOTE_ASC, VOTE_DESC};
    private SortStyle sortStyle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mProgressBar.setVisibility(View.VISIBLE);

        if (savedInstanceState == null || !savedInstanceState.containsKey("movies")) {
            if (!isNetworkAvailable()) {
                mProgressBar.setVisibility(View.GONE);
                showDialog();
            }

            mMovieList = new ArrayList<>();
            fetchData(SortStyle.POP_DESC);
        }
        else {
            mMovieList = savedInstanceState.getParcelableArrayList("movies");
            mProgressBar.setVisibility(View.GONE);
        }

        mAdapter = new SimpleItemRecyclerViewAdapter(mMovieList);

        View recyclerView = findViewById(R.id.movie_list);
        ((RecyclerView) recyclerView).setAdapter(mAdapter);

        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this, 2);
        ((RecyclerView) recyclerView).setLayoutManager(layoutManager);
        ((RecyclerView) recyclerView).setHasFixedSize(true);

        if (findViewById(R.id.movie_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if ((id == R.id.action_sort_rating) && (sortStyle != SortStyle.VOTE_DESC)) {
            fetchData(SortStyle.VOTE_DESC);
        }
        else if ((id == R.id.action_sort_popularity) && (sortStyle != SortStyle.POP_DESC)) {
            fetchData(SortStyle.POP_DESC);
        }

        return super.onOptionsItemSelected(item);
    }

    private void fetchData(SortStyle sortBy) {
        final String SORT_PARAM = "sort_by";
        final String API_KEY_PARAM = "api_key";
        final String PAGE_PARAM = "page";
        final String sortString;

        if (sortBy == SortStyle.POP_ASC) {
            sortStyle = SortStyle.POP_ASC;
            sortString = "popularity.asc";
        }
        else if (sortBy == SortStyle.POP_DESC) {
            sortStyle = SortStyle.POP_DESC;
            sortString = "popularity.desc";
        }
        else if (sortBy == SortStyle.VOTE_ASC) {
            sortStyle = SortStyle.VOTE_ASC;
            sortString = "vote_average.asc";
        }
        else {
            sortStyle = SortStyle.VOTE_DESC;
            sortString = "vote_average.desc";
        }

        mMovieList.clear();
        mProgressBar.setVisibility(View.VISIBLE);

        Uri builtUri = Uri.parse(Movie.TMDB_BASE_URL).buildUpon()
                .appendQueryParameter(SORT_PARAM, sortString)
                .appendQueryParameter(API_KEY_PARAM, "8dea3a0df3a7f2215def4ff1da625627")
                .appendQueryParameter(PAGE_PARAM, "1")
                .build();

        final OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(builtUri.toString())
                .build();

        client.newCall(request).enqueue(new com.squareup.okhttp.Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                Log.e(TAG, e.getMessage(), e);
                e.printStackTrace();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mProgressBar.setVisibility(View.GONE);
                        showDialog();
                    }
                });
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                getMoviesInfoFromJson(response.body().string());

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.notifyDataSetChanged();
                        mProgressBar.setVisibility(View.INVISIBLE);
                    }
                });
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList("movies", mMovieList);
        super.onSaveInstanceState(outState);
    }

    private void getMoviesInfoFromJson(String mJSONStr) {
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

            for (int index = 0; index < moviesArray.length(); index++) {
                JSONObject jsonObject = moviesArray.getJSONObject(index);
                String title = jsonObject.getString("original_title");
                String imagePath = jsonObject.getString("backdrop_path");
                String overview = jsonObject.getString("overview");
                String rating = jsonObject.getString("vote_average");
                String release_date = jsonObject.getString("release_date");

                Movie movie = new Movie(title, imagePath, overview, rating, release_date);
                mMovieList.add(movie);
            }
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();

        return (networkInfo != null && networkInfo.isConnected());
    }


    private void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MovieListActivity.this);
        builder.setMessage("Internet connectivity not available!");
        builder.setTitle("Information");

        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
                return;
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
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
            String url = Movie.TMDB_BASE_POSTER_URL + mValues.get(position).getImagePath();
            Picasso.with(getApplicationContext())
                    .load(url)
                    .placeholder(R.color.colorPrimary)
                    .into(holder.mImageView);

            holder.mImageView.setOnClickListener(new View.OnClickListener() {
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
            public final ImageView mImageView;

            public ViewHolder(View view) {
                super(view);
                mImageView = (ImageView) view.findViewById(R.id.image);
            }
        }
    }
}
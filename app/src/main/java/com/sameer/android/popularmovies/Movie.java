package com.sameer.android.popularmovies;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Comparator;

/**
 * Created by Sameer on 07/12/15.
 */
public class Movie implements Parcelable, Comparator, Comparable {
    public static final String TMDB_BASE_POSTER_URL = "http://image.tmdb.org/t/p/w342";
    public static final String TMDB_BASE_URL = "http://api.themoviedb.org/3/discover/movie?";
    public static final Parcelable.Creator<Movie> CREATOR = new Parcelable.Creator<Movie>() {
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };
    private String title;
    private String imagePath;
    private String overview;
    private String rating;
    private String release_date;

    public Movie(String title, String imagePath, String overview, String rating, String release_date) {
        this.imagePath = imagePath;
        this.overview = overview;
        this.rating = rating;
        this.release_date = release_date;
        this.title = title;
    }

    private Movie(Parcel in) {
        title = in.readString();
        imagePath = in.readString();
        overview = in.readString();
        rating = in.readString();
        release_date = in.readString();
    }

    public String getImagePath() {
        return imagePath;
    }

    public String getOverview() {
        return overview;
    }

    public String getRating() {
        return rating;
    }

    public String getReleaseDate() {
        return release_date;
    }

    public String getTitle() {
        return title;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(title);
        out.writeString(imagePath);
        out.writeString(overview);
        out.writeString(rating);
        out.writeString(release_date);
    }

    @Override
    public int compareTo(Object another) {
        return Double.compare(Double.parseDouble(((Movie) another).getRating()), Double.parseDouble(this.getRating()));
    }

    @Override
    public int compare(Object lhs, Object rhs) {
        return Double.compare(Double.parseDouble(((Movie) lhs).getRating()), Double.parseDouble(((Movie) rhs).getRating()));
    }
}
package com.sameer.android.popularmovies;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Sameer on 07/12/15.
 */
public class Movie implements Parcelable {
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

    public static final Parcelable.Creator<Movie> CREATOR = new Parcelable.Creator<Movie>() {
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };

    private Movie(Parcel in) {
        title = in.readString();
        imagePath = in.readString();
        overview = in.readString();
        rating = in.readString();
        release_date = in.readString();
    }
}
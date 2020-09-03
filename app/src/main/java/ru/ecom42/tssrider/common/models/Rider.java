package ru.ecom42.tssrider.common.models;

import androidx.databinding.BaseObservable;

import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Rider extends BaseObservable {
    public long id;
    @Expose
    @SerializedName("first_name")
    public String firstName;

    @Expose
    @SerializedName("last_name")
    public String lastName;

    public Media media;

    @Expose
    @SerializedName("mobile_number")
    public long mobileNumber;

    public String status;

    @Expose
    public String email;

    @Expose
    public Gender gender;

    @Expose
    @SerializedName("balance")
    private Double balance;

    @Expose
    public String address;

    public static Rider fromJson(String json) {
        return (new GsonBuilder()).create().fromJson(json, Rider.class);
    }

    public static String toJson(Rider rider) {
        return (new GsonBuilder().excludeFieldsWithoutExposeAnnotation()).create().toJson(rider);
    }
}

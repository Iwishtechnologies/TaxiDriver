// session class to store confidential information

package com.example.taxidriver.usersession;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import com.example.taxidriver.MainActivity;
import java.util.HashMap;

/**
 * Created by rrdreamtechnology on 18/01/2020.
 */

public class UserSession {

    // Shared Preferences
    SharedPreferences pref;

    // Editor for Shared preferences
    SharedPreferences.Editor editor;

    // Context
    Context context;

    // Shared pref mode
    int PRIVATE_MODE = 0;

    // Sharedpref file name
    private static final String PREF_NAME = "UserSessionPref";

    // First time logic Check
    public static final String FIRST_TIME = "firsttime";

    // All Shared Preferences Keys
    private static final String IS_LOGIN = "IsLoggedIn";

    // User name (make variable public to access from outside)
    public static final String KEY_NAME = "name";

    // Email address (make variable public to access from outside)
    public static final String KEY_EMAIL = "email";

    // Mobile number (make variable public to access from outside)
    public static final String KEY_MOBiLE = "mobile";
    // latitute number (make variable public to access from outside)
    public static final String LATITUTE = "latitute";

    // logitute number (make variable public to access from outside)
    public static final String LOGITUTE = "logitute";


    // Mobile number (make variable public to access from outside)
    public static final String DUTY_STATUS = "status";

    // user avatar (make variable public to access from outside)
    public static final String KEY_PHOTO = "photo";

    // check first time app launch
    public static final String IS_FIRST_TIME_LAUNCH = "IsFirstTimeLaunch";

    // Constructor
    public UserSession(Context context){
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }


    public void createLoginSession( String mobile){
        // Storing login value as TRUE
        editor.putBoolean(IS_LOGIN, true);

        // Storing name in pref
//        editor.putString(KEY_NAME, name);

        // Storing email in pref
//        editor.putString(KEY_EMAIL, email);

        // Storing phone number in pref
        editor.putString(KEY_MOBiLE, mobile);
        // storing duty status in pref
        editor.putString(DUTY_STATUS,"false");

        // Storing image url in pref
//        editor.putString(KEY_PHOTO, photo);

        // commit changes
        editor.commit();
    }

    /**
     * Check login method wil check user login status
     * If false it will redirect user to login page
     * Else won't do anything
     * */
    public void checkLogin(){
        // Check login status
        if(this.isLoggedIn()){
            // user is not logged in redirect him to Login Activity
            Intent i = new Intent(context, MainActivity.class);
            // Closing all the Activities
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            // Add new Flag to start new Activity
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            // Staring Login Activity
            context.startActivity(i);
        }

    }



    /**
     * Get stored session data
     * */
    public HashMap<String, String> getUserDetails(){
        HashMap<String, String> user = new HashMap<>();
        // user name
        user.put(KEY_NAME, pref.getString(KEY_NAME, null));

        // user email id
        user.put(KEY_EMAIL, pref.getString(KEY_EMAIL, null));

        // user phone number
        user.put(KEY_MOBiLE, pref.getString(KEY_MOBiLE, null));

        // user avatar
        user.put(KEY_PHOTO, pref.getString(KEY_PHOTO, null)) ;

        // return user
        return user;
    }

    /**
     * Clear session details
     * */
    public void logoutUser(){
        // Clearing all data from Shared Preferences
        editor.putBoolean(IS_LOGIN,false);
        editor.commit();

        // After logout redirect user to Login Activity
        Intent i = new Intent(context, MainActivity.class);
        // Closing all the Activities
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // Add new Flag to start new Activity
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        // Staring Login Activity
        context.startActivity(i);
    }

    /**
     * Quick check for login
     * **/
    // Get Login State
    public boolean isLoggedIn(){
        return pref.getBoolean(IS_LOGIN, false);
    }

    public Boolean getFirstTime() {
        return pref.getBoolean(FIRST_TIME, true);
    }

    public void setFirstTime(Boolean n){
        editor.putBoolean(FIRST_TIME,n);
        editor.commit();
    }

    public void setFirstTimeLaunch(boolean isFirstTime) {
        editor.putBoolean(IS_FIRST_TIME_LAUNCH, isFirstTime);
        editor.commit();
    }

    public boolean isFirstTimeLaunch() {
        return pref.getBoolean(IS_FIRST_TIME_LAUNCH, true);
    }

    /**
     *  get user mobile
     * **/
    public String get_mobile() {
        String usename = pref.getString("mobile","");
        return usename;
    }

    /**
     * set user status
     * **/
    public void set_current_duty_status(String status)
    {
        editor.putString(DUTY_STATUS,status);
        editor.commit();
    }

    /**
     * get user status
     * **/
    public String get_duty_status(){
        return pref.getString(DUTY_STATUS, null);
    }

    /**
     * set current location
     * **/
    public void setLocation(String lat,String lon)
    {
        editor.putString(LATITUTE,lat);
        editor.putString(LOGITUTE,lon);
    }

    /**
     * get user location latitute
     * **/
    public Double getLatitute()
    {
        return Double.parseDouble(pref.getString(LATITUTE,null));
    }

    /**
     * get user loaction logitute
     * **/
    public Double getLogitute()
    {
        return Double.parseDouble(pref.getString(LOGITUTE,null));
    }
}
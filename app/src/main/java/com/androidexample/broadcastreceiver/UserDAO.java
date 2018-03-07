package com.androidexample.broadcastreceiver;

import android.content.Context;

import org.json.JSONObject;

/**
 * Created by admin on 26/02/18.
 */

public class UserDAO {

        Context mContext;
        public UserDAO(Context mContext){
            this.mContext = mContext;
        }


        public org.json.JSONObject Dashboard(String requeststring)  {
            String urlString = "http://smsbooking-echs.ap-south-1.elasticbeanstalk.com/webapi/bookings/make";
            JSONObject rootObject = null;

            try {

                rootObject = WSConnection.getResult(urlString, requeststring,mContext);
            } catch (Exception e) {
              //  throw new CustomException(e.getLocalizedMessage());
            }
            return rootObject;
        }
}

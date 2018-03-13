package com.androidexample.broadcastreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import static java.util.Arrays.asList;


public class IncomingSms extends BroadcastReceiver {
    final SmsManager sms = SmsManager.getDefault();
    Context mContext;
    String finalPreftime = "";
    String senderNum;

    public static boolean isConnected(Context context) {
        ConnectivityManager
                cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null
                && activeNetwork.isConnectedOrConnecting();
    }

    public void onReceive(final Context context, Intent intent) {
        mContext = context;
        final Bundle bundle = intent.getExtras();

        try {
            if (bundle != null) {
                final Object[] pdusObj = (Object[]) bundle.get("pdus");

                for (int i = 0; i < pdusObj.length; i++) {

                    SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) pdusObj[i]);
                    String phoneNumber = currentMessage.getDisplayOriginatingAddress();

                    senderNum = phoneNumber;
                    String message = currentMessage.getDisplayMessageBody();

                    Log.i("SmsReceiver", "senderNum: " + senderNum + "; message: " + message);

                    final String[] doctorsName = {"DR.RAICHU", "DR.JOSEPH", "DR.BEENA", "DR.UMADEVI", "DR.ANJU", "DR.MANAVI", "DR.KRISHAN"};
                    String[] deptName = {"MED_SPLST", "GEN_MED", "DENTAL"};
                    int duration = Toast.LENGTH_LONG;
                    Toast toast = Toast.makeText(context, "senderNum: " + senderNum + ", message: " + message, duration);
                    toast.show();
                    if (message.toLowerCase().contains("echs_admin")) {
                        final String[] separated = message.split(",");

                        final Thread thread = new Thread(new Runnable() {

                            @Override
                            public void run() {
                                try {

                                    JSONObject submitjson = new JSONObject();
                                    submitjson.put("doctorName", separated[2].replaceAll(" ", "").toUpperCase());
                                    submitjson.put("department", separated[3].trim().toUpperCase());
                                    submitjson.put("date", separated[4].trim());
                                    Doctor_leave_service(submitjson.toString(), this);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        thread.start();
                    } else if (message.toLowerCase().contains("echs")) {
                        final String[] separated = message.split(",");
                        if (separated.length >= 5 && separated.length < 7) {
                            String preftime = separated[3].trim();
                            finalPreftime = getFinalPreferredTime(preftime.trim());

                            if (asList(deptName).contains(separated[4].trim().toUpperCase())) {

                                final Thread thread = new Thread(new Runnable() {

                                    @Override
                                    public void run() {
                                        try {

                                            JSONObject submitjson = new JSONObject();
                                            submitjson.put("serviceNumber", separated[1].trim());
                                            submitjson.put("patientName", separated[2].trim());
                                            submitjson.put("preferredTime", finalPreftime);
                                            submitjson.put("department", separated[4].trim().toUpperCase());
                                            
                                            String preferredDoctorName = separated[5].replaceAll(" ", "").toUpperCase();

                                            if (separated.length == 6 && isValidDoctorName(asList(doctorsName), preferredDoctorName)) {
//                                            asList(doctorsName).contains(separated[5].trim())) {
                                                submitjson.put("doctorName", preferredDoctorName);
                                            } else if (separated.length == 6 && !asList(doctorsName).contains(preferredDoctorName)) {
                                                SmsManager sms = SmsManager.getDefault();
                                                sms.sendTextMessage(senderNum, null, "No doctor available by that name. We are booking you with an available doctor in the specified department.", null, null);
                                            }
                                            getResult(submitjson.toString(), this);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                                thread.start();
                            } else {
                                SmsManager sms = SmsManager.getDefault();
                                sms.sendTextMessage(senderNum, null, "Invalid Department name", null, null);
                            }
                        }
                    }
                } // end for loop
            } // bundle is null

        } catch (Exception e) {
            Log.e("SmsReceiver", "Exception smsReceiver" + e);
        }
    }

    private String getFinalPreferredTime(String preftime) {
        if (preftime.equalsIgnoreCase("na")) {
            String defaultPrefTime = "10:00";
            return defaultPrefTime;
        }
        String defaultPrefTime = "10:00";
        if (preftime.contains(":") && preftime.length() == 5) {
            String replace = preftime.replace(":", "");
            try {
                Integer.valueOf(replace);
            } catch (NumberFormatException e) {
                return defaultPrefTime;
            }
            return preftime;
        } else {
            if (preftime.length() == 4 && !preftime.contains(":")) {
                try {
                    Integer.valueOf(preftime);
                } catch (NumberFormatException e) {
                    return defaultPrefTime;
                }
                preftime = preftime.substring(0, 2) + ":" + preftime.substring(2, preftime.length());
                return preftime;
            } else {
                return defaultPrefTime;
            }
        }
    }

    public void getResult(String requestStr, Runnable context) throws IOException {
        String urlString = "http://smsbooking-echs.ap-south-1.elasticbeanstalk.com/webapi/bookings/make";

        String jsonString = requestStr;
        SmsManager sms = SmsManager.getDefault();
        HttpURLConnection conn = null;

        if (isConnected(this.mContext)) {
            try {

                URL url = new URL(urlString);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setReadTimeout(15000);
                conn.setConnectTimeout(15000);
                conn.setDoInput(true);
                conn.setDoOutput(true);
                Log.e("url in WS", urlString);
                Log.e("request json", jsonString);
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter((conn.getOutputStream())));
                writer.write(jsonString, 0, jsonString.length());
                writer.flush();
                writer.close();
                StringBuilder sb = new StringBuilder();

                BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line + "\n");
                }
                br.close();
                System.out.println("SMS RESPONSE :" + sb.toString());
                JSONObject jsonObj = new JSONObject(sb.toString());
                if (jsonObj.has("errorMessage")) {
                    StringBuilder failureMessage = new StringBuilder();
                    failureMessage.append("Sorry.. Your booking could not be confirmed. Reason : ")
                            .append(jsonObj.getString("errorMessage"));
                    sms.sendTextMessage(senderNum, null, failureMessage.toString(), null, null);
                } else {
                    StringBuilder successMesage = new StringBuilder();
                    successMesage.append("Your booking with ")
                            .append(jsonObj.getString("doctorName"))
                            .append(" is confirmed for ")
                            .append(jsonObj.getString("allottedTime").replace(":", ""))
                            .append(" Hrs")
                            .append(".");
                    sms.sendTextMessage(senderNum, null, successMesage.toString(), null, null);
                }
                conn.disconnect();
            } catch (Exception e) {
                System.out.print("Response Code : " + conn.getResponseCode());
                e.printStackTrace();
            }
        }
    }


    public void Doctor_leave_service(String requestStr, Runnable context) throws IOException {
        String urlString = "http://smsbooking-echs.ap-south-1.elasticbeanstalk.com/webapi/leaves/make/";

        String jsonString = requestStr;
        SmsManager sms = SmsManager.getDefault();
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        ;

        if (isConnected(this.mContext)) {
            try {


                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setReadTimeout(15000);
                conn.setConnectTimeout(15000);
                conn.setDoInput(true);
                conn.setDoOutput(true);
                Log.e("url in WS", urlString);
                // Log.e("request json", jsonString);
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter((conn.getOutputStream())));
                writer.write(jsonString, 0, jsonString.length());
                writer.flush();
                writer.close();
                StringBuilder sb = new StringBuilder();

                BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line + "\n");
                }
                br.close();
                System.out.println("SMS RESPONSE :" + sb.toString());
                JSONObject jsonObj = new JSONObject(sb.toString());
                if (jsonObj.has("errorMessage")) {
                    StringBuilder failureMessage = new StringBuilder();
                    failureMessage.append("Sorry.. Leave could not be updated. Reason : ")
                            .append(jsonObj.getString("errorMessage"));
                    sms.sendTextMessage(senderNum, null, failureMessage.toString(), null, null);
                } else {
                    StringBuilder successMesage = new StringBuilder();
                    successMesage.append("Leave has been marked for ")
                            .append(jsonObj.getString("doctorName"))
                            .append("(").append(jsonObj.getString("department")).append(")")
                            .append(" on ")
                            .append(jsonObj.getString("date"));
                    sms.sendTextMessage(senderNum, null, successMesage.toString(), null, null);
                }
                conn.disconnect();
            } catch (Exception e) {
                System.out.print("Response Code : " + conn.getResponseCode());
                e.printStackTrace();
            }
        }
    }

    private boolean isValidDoctorName(List<String> doctorNames, String doctorName) {

        if (!doctorNames.isEmpty()) {
            for (int i = 0; i < doctorNames.size(); i++) {
                if (StringUtils.containsIgnoreCase(doctorNames.get(i), doctorName)) {
                    return true;
                } else {
                    return false;
                }
            }
        } else {
            return false;
        }
        return false;
    }


}
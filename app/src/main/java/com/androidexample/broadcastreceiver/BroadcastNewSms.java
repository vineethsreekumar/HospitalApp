package com.androidexample.broadcastreceiver;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.broadcastreceiver.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class BroadcastNewSms extends Activity {
	Uri uri = Uri.parse("content://sms/inbox");
	String dateFormat="dd-MM-yyyy";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.androidexample_broadcast_newsms);
		int PERMISSION_ALL = 1;


	}


/*

	protected boolean deletMsgFromMessageBox(Uri uri,String fromDate, String toDate,Context mContext) {
		try {
			Uri uriSms = uri;
			Calendar calCurr = Calendar.getInstance();
			Calendar calFrom = Calendar.getInstance();
			Calendar calto = Calendar.getInstance();

			calFrom.setTime((Date) new SimpleDateFormat(dateFormat)
					.parse(fromDate));
			calto.setTime((Date) new SimpleDateFormat(dateFormat).parse(toDate));



			Cursor c = mContext.getContentResolver().query(uriSms, null, null,
					null, null);

			int co = 0;
			while (c.moveToNext()) {
				calCurr.setTime((Date) new SimpleDateFormat(dateFormat)
						.parse(getDateFromCursor(c)));
				co++;

				if (calCurr.after(calFrom) && calCurr.before(calto)) {

					int id = c.getInt(0);
					int thread_id = c.getInt(1); // get the thread_id
					mContext.getContentResolver().delete(
							Uri.parse("content://sms/conversations/"
									+ thread_id),
							"thread_id=? and _id=?",
							new String[] { String.valueOf(thread_id),
									String.valueOf(id) });
				}
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

	}*/
	public void deleteSMS(View view){
		ContentResolver cr=getContentResolver();
		Uri url=Uri.parse("content://sms/");
		int num_deleted=cr.delete(url, null, null);
		Toast.makeText(this, num_deleted + " items are deleted.", Toast.LENGTH_SHORT).show();

	}

	public void getResult( String requestStr, Runnable context) {
		String urlString = "http://smsbooking-echs.ap-south-1.elasticbeanstalk.com/webapi/bookings/make";
		JSONObject submitjson = new JSONObject();
		try {
			submitjson.put("patientName", "vinunew");
			submitjson.put("doctorName", "Sreekumar");
			submitjson.put("preferredTime", "09:00");
		} catch (JSONException e) {
			e.printStackTrace();
		}

		String jsonString = submitjson.toString();



		try {

			URL url = new URL(urlString);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setRequestProperty("Content-Length", Integer.toString(jsonString.length()));
			conn.setReadTimeout(15000);
			conn.setConnectTimeout(3000);
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
			String line = null;
			while ((line = br.readLine()) != null) {
				sb.append(line + "\n");
			}
			br.close();
			System.out.println("resppppp" + sb.toString());

			conn.disconnect();
		} catch (Exception e) {

		}


	}
}

package com.cylim.saferide;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

import com.savagelook.android.UrlJsonAsyncTask;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by marco on 26/5/15.
 */
public class BackgroundCacheService extends Service {

    List<String> list_id, list_lat, list_lng, list_by, list_category;
    private static final String CACHE_END_POINT_URL = "http://saferidebymarco.herokuapp.com/api/v1/cache_reports/";
    int lastCached = 0;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        DatabaseHandler db = new DatabaseHandler(BackgroundCacheService.this);
        lastCached = db.getLastCachedReport();

        GetReportTask getReportTask = new GetReportTask(BackgroundCacheService.this);
        getReportTask.execute(CACHE_END_POINT_URL + lastCached + ".json");
    }

    private class GetReportTask extends UrlJsonAsyncTask {
        public GetReportTask(Context context) {
            super(BackgroundCacheService.this);
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            try {
                JSONArray jsonReports = json.getJSONObject("data").getJSONArray("reports");
                int length = jsonReports.length();
                List<String> reportID = new ArrayList<String>(length);
                List<String> reportLat = new ArrayList<String>(length);
                List<String> reportLng = new ArrayList<String>(length);
                List<String> reportBy = new ArrayList<String>(length);
                List<String> reportType = new ArrayList<String>(length);


                for (int i = 0; i < length; i++) {
                    reportID.add(jsonReports.getJSONObject(i).getString("id"));
                    reportLat.add(jsonReports.getJSONObject(i).getString("defects_lat"));
                    reportLng.add(jsonReports.getJSONObject(i).getString("defects_lng"));
                    reportBy.add(jsonReports.getJSONObject(i).getString("name"));
                    reportType.add(jsonReports.getJSONObject(i).getString("cat"));
                }
                list_id = new ArrayList<String>(reportID);
                list_lat = new ArrayList<String>(reportLat);
                list_lng = new ArrayList<String>(reportLng);
                list_by = new ArrayList<String>(reportBy);
                list_category = new ArrayList<String>(reportType);

            } catch (Exception e) {
                Toast.makeText(context, e.getMessage(),
                        Toast.LENGTH_LONG).show();
            } finally {
                super.onPostExecute(json);
                for (int i = 0; i < list_id.size(); i++) {
                    DatabaseHandler db = new DatabaseHandler(BackgroundCacheService.this);
                    db.cacheAddReport(Integer.parseInt(list_id.get(i)), list_lat.get(i), list_lng.get(i), list_by.get(i), list_category.get(i));
                }

            }
        }
    }
}

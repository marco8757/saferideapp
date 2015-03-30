package com.cylim.saferide;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.savagelook.android.UrlJsonAsyncTask;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by marco on 30/3/15.
 */
public class ReportLists extends Activity {

    private static final String TASKS_URL = "http://10.0.2.2:3000/api/v1/tasks.json";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.report_lists);

        loadTasksFromAPI(TASKS_URL);
    }

    private void loadTasksFromAPI(String url) {
        GetTasksTask getTasksTask = new GetTasksTask(ReportLists.this);
        getTasksTask.setMessageLoading("Loading tasks...");
        getTasksTask.execute(url);
    }

    private class GetTasksTask extends UrlJsonAsyncTask {
        public GetTasksTask(Context context) {
            super(ReportLists.this);
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            try {
                JSONArray jsonTasks = json.getJSONObject("data").getJSONArray("tasks");
                int length = jsonTasks.length();
                List<String> tasksTitles = new ArrayList<String>(length);

                for (int i = 0; i < length; i++) {
                    tasksTitles.add(jsonTasks.getJSONObject(i).getString("title"));
                }

                ListView tasksListView = (ListView) findViewById (R.id.lvR);
                if (tasksListView != null) {
                    tasksListView.setAdapter(new ArrayAdapter<String>(ReportLists.this,
                            android.R.layout.simple_list_item_1, tasksTitles));
                }
            } catch (Exception e) {
                Toast.makeText(context, e.getMessage(),
                        Toast.LENGTH_LONG).show();
            } finally {
                super.onPostExecute(json);
            }
        }
    }
}

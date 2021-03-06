package org.developfreedom.electrifrier.app;

import android.graphics.Color;
import android.graphics.Path;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.*;
import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import io.fabric.sdk.android.Fabric;
import org.eazegraph.lib.charts.BarChart;
import org.eazegraph.lib.charts.ValueLineChart;
import org.eazegraph.lib.models.BarModel;
import org.eazegraph.lib.models.ValueLinePoint;
import org.eazegraph.lib.models.ValueLineSeries;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


public class MainActivity extends AppCompatActivity implements GetResponseHandler {

    public static final String TASKS_URL = "https://a.wunderlist.com/api/v1/task_comments?task_id=1254711834";
    public static final String ISO_DATE_FORMAT = "%Y-%m-%dT%H:%M:%S.%fZ";
    public static final String ACCESS_TOKEN = "99e0b62535a0b09d9063ec6d888a8764c582bbfd50cbf70f5210271a5959";
    public static final String CLIENT_ID = "bdcd526c482653a17732";
    public static final String TAG = "MAIN_ACTIVITY";

    public static final float Safe = 10f;
    public static final float Average = 15f;
    public static final float Danger = 20f;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }

        HttpGetter httpGetter = new HttpGetter(this);
        try {
            URL taskCommentUrl = new URL(TASKS_URL);
            httpGetter.execute(taskCommentUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onGet(String response) {
        HashMap<Date, Float> data = getData(response);
        createGraph(data);
    }

    private void createGraph(HashMap<Date, Float> data) {
        //headers = {'X-Access-Token': ACCESS_TOKEN, 'X-Client-ID': CLIENT_ID}
        ValueLineChart mCubicValueLineChart = (ValueLineChart) findViewById(R.id.cubiclinechart);
        BarChart mBarChart = (BarChart) findViewById(R.id.barchart);


        ValueLineSeries series = new ValueLineSeries();
        series.setColor(0xFF56B7F1);

        ArrayList<Date> dataKeys = new ArrayList<>(data.keySet());
        Collections.sort(dataKeys);

        float lastUnit = 0;
        float daysUnits = 0;
        int lastDate = 0;
        int lastMonth = 0;

        boolean firstRun = true;

        for (Date key : dataKeys) {
            series.addPoint(new ValueLinePoint(String.format("%.2f", (float) key.getDate() + (float) key.getHours() / 24.0f), data.get(key) - 10000));

            if (firstRun){
                Log.v("Hello", "The Unit for "+key.getDate()+ " is "+data.get(key));
                lastUnit = data.get(key);
                lastDate = key.getDate();
                lastMonth = key.getMonth();
                firstRun = false;
            }

            if (lastDate < key.getDate()) {
                float difference =  data.get(key) - lastUnit;
                int color;
                if (difference > Danger) {
                    color = getResources().getColor(R.color.danger);
                }
                else if (difference > Average) {
                    color = getResources().getColor(R.color.average);
                }
                else {
                    color = getResources().getColor(R.color.safe);
                }
                mBarChart.addBar(new BarModel(lastDate+"/"+lastMonth, data.get(key) - lastUnit, color ));
                lastDate = key.getDate();
                lastMonth = key.getMonth();
                lastUnit = data.get(key);
                daysUnits = 0;
            } else {
                daysUnits = data.get(key) -  lastUnit ;
            }
            Log.v(TAG, "Data for date " + key.getDate() + " is "+ data.get(key));
        }

        if (daysUnits != 0) {
            Log.v(TAG, "Days Unit for today is " + daysUnits);
            int color;
            if (daysUnits > Danger) {
                color = getResources().getColor(R.color.danger);
            }
            else if (daysUnits > Average) {
                color = getResources().getColor(R.color.average);
            }
            else {
                color = getResources().getColor(R.color.safe);
            }
            mBarChart.addBar(new BarModel(String.valueOf(lastDate)+"/"+dataKeys.get(dataKeys.size()-1).getMonth(),daysUnits, color));
        }


        mCubicValueLineChart.addSeries(series);
        mCubicValueLineChart.startAnimation();
        mBarChart.startAnimation();
    }

    private HashMap<Date, Float> getData(String response) {
        HashMap<Date, Float> outputMap = new HashMap<>();
        Gson gson = new Gson();
        List list = gson.fromJson(response, List.class);
        for (Object obj : list) {
            if (obj instanceof LinkedTreeMap) {
                LinkedTreeMap map = (LinkedTreeMap) obj;
                Float reading = Float.parseFloat(map.get("text").toString());
                String createdAt = map.get("created_at").toString();
                try {
                    Date date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").parse(createdAt);
                    outputMap.put(date, reading);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }
        return outputMap;
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
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }
    }
}

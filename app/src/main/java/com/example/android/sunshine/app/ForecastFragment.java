package com.example.android.sunshine.app;


import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.example.android.sunshine.app.utils.Constants;
import com.example.android.sunshine.app.utils.Network;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Audi on 10/09/16.
 */
public class ForecastFragment extends Fragment {

    private ArrayAdapter<String> forecastAdapter;
    private SharedPreferences preferences;

    public ForecastFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());


        ListView lvForecast = (ListView) rootView.findViewById(R.id.lvForecast);


        forecastAdapter = new ArrayAdapter<>
                (getActivity(), R.layout.tv_list_forecast_layout, R.id.tvForecast, new ArrayList<String>());
        lvForecast.setAdapter(forecastAdapter);
        lvForecast.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String forecast = (String) adapterView.getItemAtPosition(i);
                Intent intent = new Intent(getActivity(), DetailActivity.class);
                intent.putExtra(Intent.EXTRA_TEXT, forecast);
                startActivity(intent);
            }
        });

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        updateWeather();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                updateWeather();
                return true;
            case R.id.action_settings:
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            case R.id.action_location:
                try {
                    openMaps();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
        }


        return super.onOptionsItemSelected(item);
    }

    private void openMaps() throws UnsupportedEncodingException {
        String zipCode = preferences.getString(getString(R.string.pref_title_location),getString(R.string.pref_def_location));

        Uri uri = Uri.parse("geo:0,0?")
                .buildUpon()
                .appendQueryParameter("q",zipCode)
                .build();

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(uri);
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    private void updateWeather() {
        String location = preferences.getString(getString(R.string.pref_title_location), getString(R.string.pref_def_location));
        String units = preferences.getString(getString(R.string.pref_title_temp_units),getString(R.string.pref_def_temp_units));
        new FetchWeatherTask().execute(location,units);
    }

    class FetchWeatherTask extends AsyncTask<String, Void, String[]> {

        @Override
        protected void onPostExecute(String[] strings) {
            super.onPostExecute(strings);
            if (strings != null) {
                forecastAdapter.clear();
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1)
                    forecastAdapter.addAll(Arrays.asList(strings));
                else {
                    for (String forecast : strings)
                        forecastAdapter.add(forecast);
                }

            } else {
                Toast.makeText(getActivity(), "Oops! Something went wrong..Please try again", Toast.LENGTH_LONG).show();
            }

        }

        @Override
        protected String[] doInBackground(String... strings) {
            Uri uri = new Uri.Builder()
                    .scheme("http")
                    .encodedAuthority(Constants.WEATHER_URL)
                    .appendPath("data/2.5/forecast/daily")
                    .appendQueryParameter("q", strings[0])
                    .appendQueryParameter("mode", "json")
                    .appendQueryParameter("units", strings[1])
                    .appendQueryParameter("cnt", "7")
                    .appendQueryParameter("appid", getString(R.string.app_id))
                    .build();

            Network network = new Network();
            String jsonRsp = network.sendHttpRequest(uri.toString());
            if (TextUtils.isEmpty(jsonRsp))
                return null;

            try {
                return getWeatherDataFromJson(jsonRsp, 7);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }
    }


    /* The date/time conversion code is going to be moved outside the asynctask later,
 * so for convenience we're breaking it out into its own method now.
 */
    private String getReadableDateString(long time) {
        // Because the API returns a unix timestamp (measured in seconds),
        // it must be converted to milliseconds in order to be converted to valid date.
        SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
        return shortenedDateFormat.format(time);
    }

    /**
     * Prepare the weather high/lows for presentation.
     */
    private String formatHighLows(double high, double low) {
        // For presentation, assume the user doesn't care about tenths of a degree.
        long roundedHigh = Math.round(high);
        long roundedLow = Math.round(low);

        String highLowStr = roundedHigh + "/" + roundedLow;
        return highLowStr;
    }

    /**
     * Take the String representing the complete forecast in JSON Format and
     * pull out the data we need to construct the Strings needed for the wireframes.
     * <p/>
     * Fortunately parsing is easy:  constructor takes the JSON string and converts it
     * into an Object hierarchy for us.
     */
    private String[] getWeatherDataFromJson(String forecastJsonStr, int numDays)
            throws JSONException {

        // These are the names of the JSON objects that need to be extracted.
        final String OWM_LIST = "list";
        final String OWM_WEATHER = "weather";
        final String OWM_TEMPERATURE = "temp";
        final String OWM_MAX = "max";
        final String OWM_MIN = "min";
        final String OWM_DESCRIPTION = "main";

        JSONObject forecastJson = new JSONObject(forecastJsonStr);
        JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

        // OWM returns daily forecasts based upon the local time of the city that is being
        // asked for, which means that we need to know the GMT offset to translate this data
        // properly.

        // Since this data is also sent in-order and the first day is always the
        // current day, we're going to take advantage of that to get a nice
        // normalized UTC date for all of our weather.

        Time dayTime = new Time();
        dayTime.setToNow();

        // we start at the day returned by local time. Otherwise this is a mess.
        int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

        // now we work exclusively in UTC
        dayTime = new Time();

        String[] resultStrs = new String[numDays];
        for (int i = 0; i < weatherArray.length(); i++) {
            // For now, using the format "Day, description, hi/low"
            String day;
            String description;
            String highAndLow;

            // Get the JSON object representing the day
            JSONObject dayForecast = weatherArray.getJSONObject(i);

            // The date/time is returned as a long.  We need to convert that
            // into something human-readable, since most people won't read "1400356800" as
            // "this saturday".
            long dateTime;
            // Cheating to convert this to UTC time, which is what we want anyhow
            dateTime = dayTime.setJulianDay(julianStartDay + i);
            day = getReadableDateString(dateTime);

            // description is in a child array called "weather", which is 1 element long.
            JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
            description = weatherObject.getString(OWM_DESCRIPTION);

            // Temperatures are in a child object called "temp".  Try not to name variables
            // "temp" when working with temperature.  It confuses everybody.
            JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
            double high = temperatureObject.getDouble(OWM_MAX);
            double low = temperatureObject.getDouble(OWM_MIN);

            highAndLow = formatHighLows(high, low);
            resultStrs[i] = day + " - " + description + " - " + highAndLow;
        }

//        for (String s : resultStrs) {
//            Log.v(Constants.LOG, "Forecast entry: " + s);
//        }
        return resultStrs;

    }
}

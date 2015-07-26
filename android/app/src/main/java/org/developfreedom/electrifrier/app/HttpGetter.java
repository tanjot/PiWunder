package org.developfreedom.electrifrier.app;

import android.os.AsyncTask;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;

public class HttpGetter extends AsyncTask<URL, Void, String> {
    private GetResponseHandler mHandler;

    public HttpGetter(GetResponseHandler handler) {
        this.mHandler = handler;
    }

    @Override
    protected String doInBackground(URL... params) {
        String input = null;
        URL url = params[0];
        try {
            URLConnection urlConnection = url.openConnection();
            urlConnection.setRequestProperty("X-Access-Token", MainActivity.ACCESS_TOKEN);
            urlConnection.setRequestProperty("X-Client-ID", MainActivity.CLIENT_ID);
            InputStream in;
            in = new BufferedInputStream(urlConnection.getInputStream());
            input = readInput(in);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return input;
    }

    @Override
    protected void onPostExecute(String response) {
        mHandler.onGet(response);
    }

    private String readInput(InputStream in) throws IOException {
        String lineBuffer;
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
        StringBuilder stringBuilder = new StringBuilder();
        while (((lineBuffer = bufferedReader.readLine()) != null)) {
            stringBuilder.append(lineBuffer);
        }
        return stringBuilder.toString();
    }
}

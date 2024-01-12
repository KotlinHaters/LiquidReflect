package net.ccbluex.liquidbounce.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class HttpUtils {

    private static HttpURLConnection createUrlConnection(URL url, int time) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(time);
        connection.setReadTimeout(time);
        connection.setUseCaches(false);
        return connection;
    }

    public static String getFromURL(URL url) throws IOException {
        return getFromURL(url, 20000);
    }

    public static String getFromURL(URL url, int time) throws IOException {

        HttpURLConnection connection = createUrlConnection(url, time);
        InputStream inputStream = null;
        connection.setRequestProperty("User-agent", "Mozilla/5.0 AppIeWebKit");

        String result;
        try {
            try {
                inputStream = connection.getInputStream();
                return toString(inputStream);
            } catch (IOException e) {
                closeQuietly(inputStream);
                inputStream = connection.getErrorStream();
                if (inputStream == null) throw e;
            }
            result = toString(inputStream);
        } finally {
            closeQuietly(inputStream);
        }

        return result;
    }

    public static void closeQuietly(InputStream inputStream) {
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException ignored) {
            }
        }
    }

    public static String toString(InputStream inputStream) throws IOException {
        Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8.name());
        scanner.useDelimiter("\\A");
        String result = scanner.hasNext() ? scanner.next() : "";
        scanner.close();
        return result;
    }
}

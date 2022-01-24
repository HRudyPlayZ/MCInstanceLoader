package com.hrudyplayz.mcinstanceloader.utils;

import com.hrudyplayz.mcinstanceloader.Config;
import com.hrudyplayz.mcinstanceloader.Main;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;

public class WebHelper {
// This class will allow to download a file from the internet.
// Most of it is adapted from https://github.com/Janrupf/mod-director/tree/master/mod-director-core/src/main/java/net/jan/moddirector/core/util.
// Shoutout to both Janrupf and HansWasser for making it in the first place.

    // Defines the client properties, uses common UserAgents to make every website work correctly.
    public static String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/50.0.2661.11 Safari/537.36";
    public static String REFERER = "https://www.google.com";

    private static InputStream getInputStream (URL pageURL) {
    // Returns the required InputStream by following redirects that the server may give.
    // Used by downloadFile(..., follows)

        try {
            URL url = pageURL;

            URLConnection connection = url.openConnection();

            // If there isn't any redirection, we return the actual page.
            if (!(connection instanceof HttpURLConnection)) return connection.getInputStream();

            int redirectCount = 0;

            HttpURLConnection httpConnection = (HttpURLConnection) connection;
            httpConnection.setRequestProperty("User-Agent", USER_AGENT);
            httpConnection.setRequestProperty("Referer", REFERER);
            httpConnection.connect();

            while (true) {
                int status = httpConnection.getResponseCode();
                if (status >= 300 && status <= 399) {

                    if (redirectCount > Config.maxAmountOfWebRedirections) {
                        LogHelper.error("The server at " + pageURL + "tried to redirect too many times.");
                        return null;
                    }

                    String newUrl = httpConnection.getHeaderField("Location");

                    httpConnection.getInputStream().close();
                    httpConnection.disconnect();

                    url = new URL(newUrl);
                    connection = url.openConnection();

                    if (!(connection instanceof HttpURLConnection)) {
                        LogHelper.error("The server at " + pageURL + "sent a non-http URL (" + newUrl + ")");
                        return null;
                    }

                    redirectCount += 1;

                    httpConnection = (HttpURLConnection) connection;
                    httpConnection.setRequestProperty("User-Agent", USER_AGENT);
                    httpConnection.setRequestProperty("Referer", REFERER);
                    httpConnection.connect();
                }
                else break;
            }

            return httpConnection.getInputStream();
        }

        catch (IOException e) {
            LogHelper.error("There was an issue while doing the get request to " + pageURL );
            return null;
        }

    }


    public static boolean downloadFile (String fileURL, String savePath, String[] follows) {
    // Lets you download a file from a specific URL and save it to a location.
    // Will follow any button with a text present in the follows list.

        byte[] data = null;
        URL urlToFollow = null;

        for(int i = -1; i < follows.length; i++) {

            try {

                if (i < 0) urlToFollow = new URL(fileURL);
                else {
                    String html = new String(data);

                    int startIndex = html.indexOf(follows[i]);

                    if (startIndex < 0) {
                        Main.errorContext = "Unable to find the string \"" + follows[i] + "\".";
                        return false;
                    }

                    int href = html.substring(0, startIndex).lastIndexOf("href=") + 5;
                    char hrefEnclose = html.charAt(href);
                    int hrefEnd = html.indexOf(hrefEnclose, href + 2);

                    String newUrl = html.substring(href + 1, hrefEnd);

                    if (newUrl.isEmpty()) {
                        Main.errorContext = "Result url was empty when following \"" + follows[i] + "\".";
                        return false;
                    }

                    try {
                        if (!newUrl.startsWith("http://") && !newUrl.startsWith("https://")) {
                            if (!newUrl.startsWith("/")) newUrl = "/" + newUrl;
                            urlToFollow = new URL(urlToFollow.getProtocol(), urlToFollow.getHost(), newUrl);
                        }
                        else urlToFollow = new URL(newUrl);
                    }

                    catch (MalformedURLException e) {
                        Main.errorContext = "Failed to create url when following \"" + follows[i] + "\".";
                        return false;
                    }
                }


            }

            catch (MalformedURLException e) {
                Main.errorContext = "Following the follow url lead to an invalid url.";
                return false;
            }


            try {
                InputStream inputStream = WebHelper.getInputStream(urlToFollow);
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];

                if (inputStream == null) return false;

                int read;
                while((read = inputStream.read(buffer)) > 0) outputStream.write(buffer, 0, read);

                inputStream.close();
                outputStream.close();
                data = outputStream.toByteArray();
            }

            catch(IOException e) {
                Main.errorContext = "Failed to follow the URLs to download file.";
                return false;
            }
        }

        try {
            Files.write(Paths.get(savePath), data);
        }
        catch(IOException e) {
            Main.errorContext = "There was an issue writing to file.";
            return false;
        }

        return true;
    }


    public static boolean downloadFile (String fileURL, String savePath) {
    // Lets you download a file from a specific URL and save it to a location.

        try {
            URL url = new URL(fileURL);

            URLConnection connection = url.openConnection();
            if (!(connection instanceof HttpURLConnection)) {
                // If there isn't any redirect, download the file directly.

                FileUtils.copyInputStreamToFile(connection.getInputStream(), new File(savePath));
                return true;
            }

            int redirectCount = 0;

            HttpURLConnection httpConnection = (HttpURLConnection) connection;
            httpConnection.setRequestProperty("User-Agent", USER_AGENT);
            httpConnection.setRequestProperty("Referer", REFERER);
            httpConnection.connect();

            while (true) {
                int status = httpConnection.getResponseCode();
                if (status >= 300 && status <= 399) {
                    if (redirectCount > Config.maxAmountOfWebRedirections) {
                        Main.errorContext = "The server tried to redirect too many times.";
                        return false;
                    }

                    String newUrl = httpConnection.getHeaderField("Location");

                    httpConnection.getInputStream().close();
                    httpConnection.disconnect();

                    url = new URL(newUrl);
                    connection = url.openConnection();

                    if (!(connection instanceof HttpURLConnection)) {
                        Main.errorContext = "The server sent a non-http URL.";
                        return false;
                    }

                    redirectCount += 1;

                    httpConnection = (HttpURLConnection) connection;
                    httpConnection.setRequestProperty("User-Agent", USER_AGENT);
                    httpConnection.setRequestProperty("Referer", REFERER);
                    httpConnection.connect();
                }
                else break;
            }

            FileUtils.copyInputStreamToFile(httpConnection.getInputStream(), new File(savePath));
            return true;
        }
        catch (IOException e) {
            Main.errorContext = "There was an issue writing to file.";
            return false;
        }

    }
}

package com.hrudyplayz.mcinstanceloader.utils;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

import com.hrudyplayz.mcinstanceloader.Config;
import com.hrudyplayz.mcinstanceloader.Main;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
An helper class to download files from the internet.

@author HRudyPlayZ
*/
@SuppressWarnings("unused")
public class WebHelper {

    // Defines the client properties, uses Twitch UserAgents to make every website work correctly as they should.
    public static String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) twitch-desktop-electron-platform/1.0.0 Chrome/73.0.3683.121 Electron/5.0.12 Safari/537.36 desklight/8.51.0";
    public static String REFERER = "https://www.google.com";

    public static Boolean checkSSLCertificates = true;

    /**
    Downloads a specific file from an internet address and saves it to a given location.

    @param fileURL The URL of the file.
    @param savePath Where to save the downloaded file on the disk.
    @return Whether the operation succeeded or not.
    */
    public static boolean downloadFile(String fileURL, String savePath) {
        return downloadFile(fileURL, savePath, false);
    }

    private static boolean downloadFile(String fileURL, String savePath, boolean doneIOException) {
        URL url;
        try {
            url = new URL(fileURL);
        }
        catch (Exception e) {
            Main.errorContext = "The URL is invalid.";
            return false;
        }

        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setInstanceFollowRedirects(true);
            
            connection.setReadTimeout(Config.connectionTimeout * 1000);
            connection.setConnectTimeout(Config.connectionTimeout * 1000);

            connection.setRequestProperty("User-Agent", USER_AGENT);
            connection.setRequestProperty("Referer", REFERER);
            if (Config.curseforgeAPIKey.length() > 0) connection.setRequestProperty("x-api-key", Config.curseforgeAPIKey);

            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                Main.errorContext = "Received an HTTP " + responseCode + " error.";
                return false;
            }

            File file = new File(savePath);
            FileUtils.copyInputStreamToFile(connection.getInputStream(), file);
            file.canRead();
            if (doneIOException)
                LogHelper.info("Successfully downloaded file on the second attempt.");
            return true;
        }
        catch (IOException e) {
            if (!doneIOException) {
                LogHelper.info("An error occurred while downloading the file, trying again...");
                try {
                    TimeUnit.MILLISECONDS.sleep(1000);
                }
                catch (InterruptedException ignore) {}

                if (Config.allowSSLCertificateBypass && checkSSLCertificates) {
                    LogHelper.info("Attempting to resolve issues by bypassing SSL certificates.");
                    disableSSLCertificateChecking();
                }

                return downloadFile(fileURL, savePath, true);
            }
            else {

                Main.errorContext = "There was an issue writing to file.";
                return false;
            }
        }
    }


    /**
    Downloads a specific file from an internet address and saves it to a given location.
    It will first follow a list of buttons on the page to get to the final URL of the file.
    To get to the final URL, this function will try to simulate a click on every HTML object with a given text, in order.
    To do that, it will follow the href of such objects.

    @param fileURL The URL of the starting page.
    @param savePath Where to save the downloaded file on the disk.
    @param follows A list of buttons to follow in the right order.
    @return Whether the operation succeeded or not.
    */
    public static boolean downloadFile(String fileURL, String savePath, String[] follows) {
    // Lets you download a file from a specific URL and save it to a location.
    // Will follow any button with a text present in the follows list.

        int counter = 0;
        while (counter < follows.length) {
            if (!downloadFile(fileURL, Config.configFolder + "temp" + File.separator + "page.html")) {
                Main.errorContext = "Error downloading the redirected " + follows[counter] + " page.";
                return false;
            }

            String[] lines = FileHelper.listLines(Config.configFolder + "temp" + File.separator + "page.html");
            String html = "";
            for (String s : lines) html += s;

            // This parses the HTML file using regex.
            // Thanks to the Melody language for letting us generate regex using a readable syntax :P
            String[] splitted = html.split(">\\s*" + Pattern.quote(follows[counter]) + "\\s*<");
            if (splitted.length == 1) {
                Main.errorContext = "Unable to find the string \"" + follows[counter] + "\".";
                return false;
            }

            String href = splitted[0].replaceAll("'", "\"").replaceAll("\\s", "");
            href = href.substring(href.lastIndexOf("href") + 6);
            href = href.substring(0, href.indexOf("\""));

            try {
                if (href.startsWith("http://") || href.startsWith("https://")) fileURL = href;
                else {
                    URL url = new URL(fileURL);
                    fileURL = url.getProtocol() + "://" + url.getHost() + "/" + href;
                }
            }
            catch (Exception e) {
                Main.errorContext = "Following " + follows[counter] + " lead to an invalid URL.";
                return false;
            }

            counter += 1;
        }

        return downloadFile(fileURL, savePath);
    }

    /**
     * Disables the SSL certificate checking for new instances of {@link HttpsURLConnection} This has been created to
     * aid testing on a local box, not for use on production.
     */
    private static void disableSSLCertificateChecking() {
        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            @Override
            public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                // Not implemented
            }

            @Override
            public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                // Not implemented
            }
        } };

        try {
            SSLContext sc = SSLContext.getInstance("TLS");

            sc.init(null, trustAllCerts, new java.security.SecureRandom());

            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (KeyManagementException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        checkSSLCertificates = false;
    }
}

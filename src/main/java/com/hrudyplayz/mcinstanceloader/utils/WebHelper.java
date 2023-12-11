package com.hrudyplayz.mcinstanceloader.utils;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import javax.net.ssl.*;
import org.apache.commons.io.FileUtils;

import com.hrudyplayz.mcinstanceloader.Config;
import com.hrudyplayz.mcinstanceloader.Main;


/**
An helper class to download files from the internet.

@author HRudyPlayZ
*/
@SuppressWarnings("unused")
public class WebHelper {

    // Defines the client properties, uses Twitch UserAgents to make every website work correctly as they should.
    public static String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) twitch-desktop-electron-platform/1.0.0 Chrome/73.0.3683.121 Electron/5.0.12 Safari/537.36 desklight/8.51.0";
    public static String REFERER = "https://www.google.com";
    static String[] ordinalSuffixes = {
            "st",
            "nd",
            "rd",
            "th"
    };

    private static final SSLSocketFactory DEFAULT_HTTPS_CERTIFICATES = HttpsURLConnection.getDefaultSSLSocketFactory();

    /**
    Downloads a specific file from an internet address and saves it to a given location.

    @param fileURL The URL of the file.
    @param savePath Where to save the downloaded file on the disk.
    @return Whether the operation succeeded or not.
    */
    public static boolean downloadFile(String fileURL, String savePath) {
        return downloadFile(fileURL, savePath, 0);
    }
    private static boolean downloadFile(String fileURL, String savePath, int retryCount) {
        // Resets the SSL certificate ignore status, for the upcoming HttpURLConnection
        if (retryCount == 0) toggleHTTPSCertificateChecks(true);

        // URL Object used for the URLConnection
        URL url;
        try {
            url = new URL(fileURL);
        }
        catch (Exception e) {
            Main.errorContext = "The URL is invalid.";
            return false;
        }

        try {
            // URLConnection object
            HttpURLConnection connection;
            if (!Config.httpProxyHost.isEmpty()) {
                Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(Config.httpProxyHost, Config.httpProxyPort));
                connection = (HttpURLConnection) url.openConnection(proxy);
            }
            else connection = (HttpURLConnection) url.openConnection();

            // GET Method that follows redirects
            connection.setRequestMethod("GET");
            connection.setInstanceFollowRedirects(true);

            // Timeouts
            connection.setReadTimeout(Config.connectionTimeout * 1000);
            connection.setConnectTimeout(Config.connectionTimeout * 1000);

            // Request properties
            connection.setRequestProperty("User-Agent", USER_AGENT);
            connection.setRequestProperty("Referer", REFERER);

            if (!Config.curseforgeAPIKey.isEmpty()) connection.setRequestProperty("x-api-key", Config.curseforgeAPIKey);

            // If the response didn't an HTTP 200 (OK) response code, it failed.
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                if (retryCount < Config.maxAmountOfDownloadRetries) {
                    LogHelper.info("The website returned an HTTP " + responseCode + " error status, trying again...");

                    try {
                        TimeUnit.MILLISECONDS.sleep(1000);
                    }
                    catch (InterruptedException ignore) {}

                    return downloadFile(fileURL, savePath, retryCount + 1);
                }
                else {
                    Main.errorContext = "Received an HTTP " + responseCode + " error.";
                    return false;
                }
            }

            File file = new File(savePath);
            FileUtils.copyInputStreamToFile(connection.getInputStream(), file);

            if (retryCount > 0) {
                String ordinal = retryCount % 10 < 3 ? ordinalSuffixes[(retryCount % 10)] : ordinalSuffixes[3];

                LogHelper.info("Successfully downloaded file on " + (retryCount + 1) + ordinal +  " attempt.");
            }

            return true;
        }
        catch (SSLException e) {
            if (Config.allowHTTPSCertificateCheckBypass && retryCount < Config.maxAmountOfDownloadRetries) {
                LogHelper.info("An error occurred while checking the HTTPS certificate of the address, disabling the certificate check and trying again...");

                toggleHTTPSCertificateChecks(false);

                try {
                    TimeUnit.MILLISECONDS.sleep(1000);
                }
                catch (InterruptedException ignore) {}

                return downloadFile(fileURL, savePath, retryCount + 1);
            }
            else {
                Main.errorContext = "There was an issue writing to file.";
                e.printStackTrace();
                return false;
            }
        }
        catch (IOException e) {
            if (retryCount < Config.maxAmountOfDownloadRetries) {
                LogHelper.info("An error occurred while downloading the file, trying again...");

                try {
                    TimeUnit.MILLISECONDS.sleep(1000);
                }
                catch (InterruptedException ignore) {}

                return downloadFile(fileURL, savePath, retryCount + 1);
            }
            else {
                Main.errorContext = "There was an issue writing to file.";
                e.printStackTrace();
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
     Enables or disables the TLS/SSL certificate checks of future {@link HttpURLConnection} instances

     @param enableChecks Whether to enable or disable the TLS/SSL certificate checks
    */
    private static void toggleHTTPSCertificateChecks(boolean enableChecks) {
        if (enableChecks) HttpsURLConnection.setDefaultSSLSocketFactory(DEFAULT_HTTPS_CERTIFICATES);
        else {
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }

                        @Override
                        public void checkClientTrusted(X509Certificate[] arg0, String arg1) {}

                        @Override
                        public void checkServerTrusted(X509Certificate[] arg0, String arg1) {}
                    }
            };

            try {
                SSLContext context = SSLContext.getInstance("TLS");

                context.init(null, trustAllCerts, new java.security.SecureRandom());
                HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());
            }
            catch (KeyManagementException | NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }
    }
}

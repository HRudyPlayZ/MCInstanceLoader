package com.hrudyplayz.mcinstanceloader.utils;

import com.hrudyplayz.mcinstanceloader.Config;
import com.hrudyplayz.mcinstanceloader.Main;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;

import java.io.*;
import java.net.*;
import java.util.regex.Pattern;


@SuppressWarnings("unused")
public class WebHelper {
// This class will allow to download a file from the internet.
// Most of it is adapted from https://github.com/Janrupf/mod-director/tree/master/mod-director-core/src/main/java/net/jan/moddirector/core/util.
// Shoutout to both Janrupf and HansWasser for making it in the first place.

    // Defines the client properties, uses Twitch UserAgents to make every website work correctly as they should.
    public static String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) twitch-desktop-electron-platform/1.0.0 Chrome/73.0.3683.121 Electron/5.0.12 Safari/537.36 desklight/8.51.0";
    public static String REFERER = "https://www.google.com";

    public static boolean downloadFile (String fileURL, String savePath) {
    // Lets you download a file from a specific URL and save it to a location.

        RequestConfig timeouts = RequestConfig.custom() // Creates the request config to set the custom timeout values
                                              .setConnectTimeout(Config.connectionTimeout * 1000)
                                              .setConnectionRequestTimeout(Config.connectionTimeout * 1000)
                                              .setSocketTimeout(Config.connectionTimeout * 1000)
                                              .build();

        CloseableHttpClient client = HttpClients.custom() // Creates an httpClient with custom properties
                                                .setUserAgent(USER_AGENT) // Adds the user-agent
                                                .setRedirectStrategy(new LaxRedirectStrategy()) // Allows for redirect handling
                                                .setDefaultRequestConfig(timeouts)
                                                .disableContentCompression().build(); // Disables compression (slightly faster and more reliable downloads) and builds the client.

        try {
            HttpGet request = new HttpGet(new URL(fileURL).toURI()); // Creates the GET request.
            request.addHeader("Referer", REFERER); // Adds the referer to the request.

            if (Config.curseforgeAPIKey.length() > 0) request.addHeader("x-api-key", Config.curseforgeAPIKey);

            HttpEntity entity = client.execute(request).getEntity();
            if (entity != null) FileUtils.copyInputStreamToFile(entity.getContent(), new File(savePath));

            return true;
        }
        catch (URISyntaxException e) {
            Main.errorContext = "The URL is invalid.";
            return false;
        }
        catch (ClientProtocolException e) {
            Main.errorContext = "HTTP Protocol violation.";
            return false;
        }
        catch (IOException e) {
            Main.errorContext = "There was an issue writing to file.";
            return false;
        }
        finally {
            IOUtils.closeQuietly(client);
        }
    }


    public static boolean downloadFile (String fileURL, String savePath, String[] follows) {
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

            String matched = ">" + follows[counter] + "<";

            // Yeah i parse HTML with regex, what are you gonna do? Kill me? x)
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
                URL url = new URL(fileURL);
                fileURL = url.getProtocol() + "://" + url.getHost() + "/" + href;
            }
            catch (Exception e) {
                Main.errorContext = "Following " + follows[counter] + " lead to an invalid URL.";
                return false;
            }

            if (fileURL.isEmpty()) {
                Main.errorContext = "Result URL was empty when following " + follows[counter] + ".";
                return false;
            }

            counter += 1;
        }

        return downloadFile(fileURL, savePath);
    }
}

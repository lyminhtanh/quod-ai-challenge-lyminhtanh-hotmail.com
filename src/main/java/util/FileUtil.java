package util;

import constant.Constant;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.IllegalFormatException;
import java.util.zip.GZIPInputStream;

public class FileUtil {
    /**
     * download File
     * @param dateTimeString
     */
    public static void downloadAsJsonFile(final String dateTimeString) {
        try {
            final String pathname = String.format(Constant.BASE_UNZIPPED_FILE_NAME, dateTimeString);
            System.out.println(String.format("--Downloading: %s", String.format(Constant.BASE_URL, dateTimeString)));
            final File downloadedFile = new File(pathname);

            final URLConnection conn = new URL(String.format(Constant.BASE_URL, dateTimeString)).openConnection();

            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:31.0) Gecko/20100101 Firefox/31.0");
            conn.connect();

            // decompress gzip then saving to file
            FileUtils.copyInputStreamToFile(new GZIPInputStream(conn.getInputStream()), downloadedFile);

            System.out.println(String.format("-- Saved to: %s.json", dateTimeString));
        } catch (IOException | IllegalFormatException ex) {
            System.out.println(String.format("Failed to download file. %s", ex));
        }
    }



}

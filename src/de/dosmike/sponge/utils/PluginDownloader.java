package de.dosmike.sponge.utils;

import de.dosmike.sponge.oreapi.OreApiV2;
import de.dosmike.sponge.oreapi.v2.OreProject;
import de.dosmike.sponge.oreapi.v2.OreVersion;

import javax.net.ssl.HttpsURLConnection;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PluginDownloader extends Thread implements Runnable {

    long file_loaded=0L;
    OreProject project;
    OreVersion version;
    File localFile;
    boolean success=false;
    boolean done=false;
    OreApiV2 apiInstance;
    public PluginDownloader(OreApiV2 apiInstance, OreProject project, OreVersion version) {
        this.project = project;
        this.version = version;
        this.apiInstance = apiInstance;
    }

    /** TODO display a disclaimer before downloading a file! (To be done somewhere before this) */
    @Override
    public void run() {
        URL targetUrl = apiInstance.getDownloadURL(project.getNamespace(), version);
        if (targetUrl==null) {
            done = true;
            return;
        }
        FileOutputStream fos=null;
        InputStream in=null;
        try {
            HttpsURLConnection connection = (HttpsURLConnection) targetUrl.openConnection();
            connection.setInstanceFollowRedirects(true);
            connection.setRequestMethod("GET");
//            connection.setRequestProperty("Accept", "application/octet-stream");
            connection.setRequestProperty("Accept", "*/*");
            connection.setRequestProperty("User-Agent", "OreGet (by DosMike)/1.0");
            apiInstance.getSession().authenticate(connection);
            connection.setDoInput(true);
            if (connection.getResponseCode() < 200 || connection.getResponseCode() >= 400) {
                OreApiV2.tryPrintErrorBody(connection);
                return;
            }
            //extract filname from content-disposition header
            String filename = connection.getHeaderFields().entrySet().stream()
                    .filter((entry)->"content-disposition".equalsIgnoreCase(entry.getKey()))
                    .map(entry->entry.getValue().stream()
                            .filter(v->v.contains("filename="))
                            .findFirst()
                            .map(header->{
                                String[] parts = header.split(";");
                                for (String part : parts) {
                                    int index = part.indexOf("filename=");
                                    if (index >= 0)
                                        return part.substring(index+9);
                                }
                                return "";
                            })
                            .orElse(""))
                    .findFirst().orElse("");
            if (filename.isEmpty()) return;
            if (filename.startsWith("\"") && filename.endsWith("\""))
                filename = filename
                        //strip quotes
                        .substring(1, filename.length()-1)
                        //unescape quoted-pairs as noticed in https://www.ietf.org/rfc/rfc2616.txt
                        //  -> 4.2 Message Headers  -> 2.2 Basic Rules  -> quoted-string
                        .replaceAll("\\\\(.)", "$1");

            localFile = new File("oreget_cache/", filename);
            fos = new FileOutputStream(localFile);
            in = connection.getInputStream();
//            Base64.getDecoder().wrap(in);
            MessageDigest hasher = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[1024]; int r;
            while ((r=in.read(buffer))>0) {
                fos.write(buffer, 0, r);
                hasher.update(buffer, 0, r);
                file_loaded += r;
            }
            byte[] rawhash = hasher.digest();
            StringBuilder hashBuilder = new StringBuilder();
            for (byte b : rawhash)
                hashBuilder.append(String.format("%02X", ((int)b&0xFF)));

            success = hashBuilder.toString().equalsIgnoreCase(version.getFileMD5());
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        } finally {
            done = true;
            try { fos.flush(); } catch (Exception ignore) {}
            try { fos.close(); } catch (Exception ignore) {}
            try { in.close(); } catch (Exception ignore) {}
        }
    }

    /** @return the downloaded file on success */
    public Optional<File> target() {
        return success ? Optional.of(localFile) : Optional.empty();
    }

    public float getProgress() {
        return (float)file_loaded/version.getFileSize();
    }
    public boolean isDone() {
        return done;
    }

    public void createDisplay() {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        Runnable updateDisplay = ()->{
            String update = String.format("Downloading %s... %.2f%%", project.getPluginId(), getProgress()*100);
            System.out.println(update);
            if (isDone())
                executor.shutdownNow();
        };
        executor.scheduleAtFixedRate(updateDisplay, 5, 2, TimeUnit.SECONDS);
    }
}

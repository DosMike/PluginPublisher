package de.dosmike.sponge.utils;

import javax.net.ssl.HttpsURLConnection;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

/**
 * In the latest Sponge Update {@see https://forums.spongepowered.org/t/sponge-status-update-12th-december-2019/34368}
 * the Sponge team decided that for your Plugins to be approved, the following conditions must be also be met by the project:
 * <ul>
 * <li>Downloaded content must have hard-coded SHA256 (or better) based hash checking
 * <li>Downloaded content must be explained in the main project page as to what is downloaded and what purpose it serves
 * <li>Downloaded content must be performed over HTTPS connections
 * <li>Downloaded content must not be hosted in a location that will limit downloads (e.g. DropBox, Google Drive)
 * </ul>
 * This GIST is supposed to help you with hashing files you want to download.
 */
public class SpongeDownload {

    /* THIS IS NOT AN EXHAUSTIVE LIST! THERE ARE MORE ALGORITHMS
     * THAT MEET THE SPONGE CRITERIA, BUT THOSE ARE PROBABLY THE
     * MOST COMMON */
    public static final String ALGORITHM_SHA256 = "SHA-256";
    public static final String ALGORITHM_SHA384 = "SHA-384";
    public static final String ALGORITHM_SHA512 = "SHA-512";
    /**
     * Provides a default user agent for the remote connection.
     * It's advised that every application (or in this case plugin)
     * provides their own unique user agent so that remote servers
     * can react in case your application sends bad requests.
     * @return a default UserAgent-String containing Platform information
     */
    private static String getDefaultUserAgent() {
        return "Generic OreAPI V2 Implementation/1.0 (by DosMike)";
    }

    /**
     * To notify that the hash did not match. can provide the local Path, expected hash
     * and result hash for custom error printing
     */
    public static class InvalidHashException extends Exception {
        private Path local;
        private String expected, received;
        public InvalidHashException(String message, Path local, String expected, String received) {
            super(message);
            this.local = local;
            this.expected = expected;
            this.received = received;
        }
        /** @return the hash string supplied by you */
        public String getExpectedHash() { return expected; }
        /** @return the hash string that was calculated for this file */
        public String getCalculatedHash() { return received; }
        /** @return the download target (should not exist) */
        public Path getLocalPath() { return local; }
    }

    /**
     * Downloads a file and verifies the hash with the specified algorithm.
     * If the download fails or the hash does not end up matching the file will be deleted again.
     * The file may nor exist when calling this function to prevent unintentional overwriting.
     * This method will follow redirects if the URL returns a 3XX response code.
     * A default UserAgent will be supplied (see {@link #getDefaultUserAgent()})
     * @param remote the URL to where the file is located in the internet, has to use protocol https
     * @param local where to download the file to. This has to include filename and extension and the file may not already exist.
     * @param sha256 the human readable representation of thr SHA-256 for this file, so you can copy/paste it from third party hash tools.
     * @throws IllegalArgumentException if the Protocol for remote was not https
     * @throws java.io.IOError if the local file seems to be invalid
     * @throws FileAlreadyExistsException if the local file already exists, or a path element prior to the file name is a file (see {@link Files#createDirectories})
     * @throws NoSuchAlgorithmException if the supplied hashMethod is invalid or can't be instantiated
     * @throws IOException if an error occurs while reading the remote or writing the local file
     * @throws InvalidHashException if the hash for the downloaded file did not match the supplied hash
     * @return the Path to the downloaded file
     */
    public static Path downloadAndVerify(URL remote, Path local, String sha256) throws IOException, NoSuchAlgorithmException, InvalidHashException {
        return downloadAndVerify(remote, getDefaultUserAgent(), local, sha256, ALGORITHM_SHA256);
    }
    /**
     * Downloads a file and verifies the hash with the specified algorithm.
     * If the download fails or the hash does not end up matching the file will be deleted again.
     * The file may nor exist when calling this function to prevent unintentional overwriting.
     * This method will follow redirects if the URL returns a 3XX response code.
     * @param remote the URL to where the file is located in the internet, has to use protocol https
     * @param userAgent a custom user agent to identify your plugin/application
     * @param local where to download the file to. This has to include filename and extension and the file may not already exist.
     * @param sha256 the human readable representation of thr SHA-256 for this file, so you can copy/paste it from third party hash tools.
     * @throws IllegalArgumentException if the Protocol for remote was not https
     * @throws java.io.IOError if the local file seems to be invalid
     * @throws FileAlreadyExistsException if the local file already exists, or a path element prior to the file name is a file (see {@link Files#createDirectories})
     * @throws NoSuchAlgorithmException if the supplied hashMethod is invalid or can't be instantiated
     * @throws IOException if an error occurs while reading the remote or writing the local file
     * @throws InvalidHashException if the hash for the downloaded file did not match the supplied hash
     * @return the Path to the downloaded file
     */
    public static Path downloadAndVerify(URL remote, String userAgent, Path local, String sha256) throws IOException, NoSuchAlgorithmException, InvalidHashException {
        return downloadAndVerify(remote, userAgent, local, sha256, ALGORITHM_SHA256);
    }
    /**
     * Downloads a file and verifies the hash with the specified algorithm.
     * If the download fails or the hash does not end up matching the file will be deleted again.
     * The file may nor exist when calling this function to prevent unintentional overwriting.
     * This method will follow redirects if the URL returns a 3XX response code.
     * A default UserAgent will be supplied (see {@link #getDefaultUserAgent()})
     * @param remote the URL to where the file is located in the internet, has to use protocol https
     * @param local where to download the file to. This has to include filename and extension and the file may not already exist.
     * @param hashString the human readable representation of the expected hash for this file, so you can copy/paste it from third party hash tools.
     * @param hashMethod the name of the hashing algorithm used. Note that Sponge requires SHA-256 or better!
     * @throws IllegalArgumentException if the Protocol for remote was not https
     * @throws java.io.IOError if the local file seems to be invalid
     * @throws FileAlreadyExistsException if the local file already exists, or a path element prior to the file name is a file (see {@link Files#createDirectories})
     * @throws NoSuchAlgorithmException if the supplied hashMethod is invalid or can't be instantiated
     * @throws IOException if an error occurs while reading the remote or writing the local file
     * @throws InvalidHashException if the hash for the downloaded file did not match the supplied hash
     * @return the Path to the downloaded file
     */
    public static Path downloadAndVerify(URL remote, Path local, String hashString, String hashMethod) throws IOException, NoSuchAlgorithmException, InvalidHashException {
        return downloadAndVerify(remote, getDefaultUserAgent(), local, hashString, hashMethod);
    }
    /**
     * Downloads a file and verifies the hash with the specified algorithm.
     * If the download fails or the hash does not end up matching the file will be deleted again.
     * The file may nor exist when calling this function to prevent unintentional overwriting.
     * This method will follow redirects if the URL returns a 3XX response code.
     * @param remote the URL to where the file is located in the internet, has to use protocol https
     * @param userAgent a custom user agent to identify your plugin/application
     * @param local where to download the file to. This has to include filename and extension and the file may not already exist.
     * @param hashString the human readable representation of the expected hash for this file, so you can copy/paste it from third party hash tools.
     * @param hashMethod the name of the hashing algorithm used. Note that Sponge requires SHA-256 or better!
     * @throws IllegalArgumentException if the Protocol for remote was not https
     * @throws java.io.IOError if the local file seems to be invalid
     * @throws FileAlreadyExistsException if the local file already exists, or a path element prior to the file name is a file (see {@link Files#createDirectories})
     * @throws NoSuchAlgorithmException if the supplied hashMethod is invalid or can't be instantiated
     * @throws IOException if an error occurs while reading the remote or writing the local file
     * @throws InvalidHashException if the hash for the downloaded file did not match the supplied hash
     * @return the Path to the downloaded file
     */
    public static Path downloadAndVerify(URL remote, String userAgent, Path local, String hashString, String hashMethod) throws IOException, NoSuchAlgorithmException, InvalidHashException {
        Map<String, String> headers = new HashMap<>();
        headers.put("UserAgent", userAgent);
        return downloadAndVerify(remote, headers, local, hashString, hashMethod);
    }
    /**
     * Downloads a file and verifies the hash with the specified algorithm.
     * If the download fails or the hash does not end up matching the file will be deleted again.
     * The file may nor exist when calling this function to prevent unintentional overwriting.
     * This method will follow redirects if the URL returns a 3XX response code.
     * @param remote the URL to where the file is located in the internet, has to use protocol https
     * @param requestHeaders a map to supply custom headers that websites may require, UserAgent is strongly recommended
     * @param local where to download the file to. This has to include filename and extension and the file may not already exist.
     * @param sha256 the human readable representation of thr SHA-256 for this file, so you can copy/paste it from third party hash tools.
     * @throws IllegalArgumentException if the Protocol for remote was not https
     * @throws java.io.IOError if the local file seems to be invalid
     * @throws FileAlreadyExistsException if the local file already exists, or a path element prior to the file name is a file (see {@link Files#createDirectories})
     * @throws NoSuchAlgorithmException if the supplied hashMethod is invalid or can't be instantiated
     * @throws IOException if an error occurs while reading the remote or writing the local file
     * @throws InvalidHashException if the hash for the downloaded file did not match the supplied hash
     * @return the Path to the downloaded file
     */
    public static Path downloadAndVerify(URL remote, Map<String, String> requestHeaders, Path local, String sha256) throws IOException, NoSuchAlgorithmException, InvalidHashException {
        return downloadAndVerify(remote, requestHeaders, local, sha256, ALGORITHM_SHA256);
    }
    /**
     * Downloads a file and verifies the hash with the specified algorithm.
     * If the download fails or the hash does not end up matching the file will be deleted again.
     * The file may nor exist when calling this function to prevent unintentional overwriting.
     * This method will follow redirects if the URL returns a 3XX response code.
     * @param remote the URL to where the file is located in the internet, has to use protocol https
     * @param requestHeaders a map to supply custom headers that websites may require, UserAgent is strongly recommended
     * @param local where to download the file to. This has to include filename and extension and the file may not already exist.
     * @param hashString the human readable representation of the expected hash for this file, so you can copy/paste it from third party hash tools.
     * @param hashMethod the name of the hashing algorithm used. Note that Sponge requires SHA-256 or better!
     * @throws IllegalArgumentException if the Protocol for remote was not https
     * @throws java.io.IOError if the local file seems to be invalid
     * @throws FileAlreadyExistsException if the local file already exists, or a path element prior to the file name is a file (see {@link Files#createDirectories})
     * @throws NoSuchAlgorithmException if the supplied hashMethod is invalid or can't be instantiated
     * @throws IOException if an error occurs while reading the remote or writing the local file
     * @throws InvalidHashException if the hash for the downloaded file did not match the supplied hash
     * @return the Path to the downloaded file
     */
    public static Path downloadAndVerify(URL remote, Map<String, String> requestHeaders, Path local, String hashString, String hashMethod) throws IOException, NoSuchAlgorithmException, InvalidHashException {

        //ensure downloads are done via https
        if (!"https".equalsIgnoreCase(remote.getProtocol()))
            throw new IllegalArgumentException("You're not allowed to download over insecure connections, use https://");

        //check file does not already exist
        local = local.toAbsolutePath();
        if (Files.exists(local))
            throw new FileAlreadyExistsException("This implementation won't replace files, please remove the existing file before downloading: "+local.toString());
        //make sure directories are there
        Files.createDirectories(local.getParent());

        //prepare hash algorithm (and check existence)
        MessageDigest hasher = MessageDigest.getInstance(hashMethod);

        //setup secure https connectiono
        HttpsURLConnection connection = (HttpsURLConnection) remote.openConnection();
        requestHeaders.forEach(connection::setRequestProperty);
        connection.setDoInput(true);
        connection.setReadTimeout(5000);
        connection.setConnectTimeout(5000);
        connection.setInstanceFollowRedirects(true);
        //validate successful connection
        int responseCode = connection.getResponseCode();
        if (responseCode < 200 || responseCode >= 400)
            throw new IOException("Connection failed with response "+responseCode+": "+connection.getResponseMessage());

        //allocate streams
        InputStream in = null;
        OutputStream out = null;

        boolean success = false;
        try {
            in = connection.getInputStream();
            out = new FileOutputStream(local.toFile());
            //download and hash
            byte[] buffer = new byte[1024]; int r;
            while ((r=in.read(buffer))>=0) {
                out.write(buffer,0,r);
                hasher.update(buffer,0,r);
            }
            //create hash string from digest
            byte[] rawHash = hasher.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : rawHash) {
                sb.append(String.format("%02X", ((int)b&0xFF)));
            }
            String rawAsString = sb.toString();
            //validate hash
            success = rawAsString.equalsIgnoreCase(hashString);
            if (!success)
                throw new InvalidHashException("The hash did not match: "+hashString+" vs "+rawAsString, local, hashString, rawAsString);
        } finally {
            //close streams
            try { in.close(); } catch (Exception ignore) {}
            try { out.flush(); } catch (Exception ignore) {}
            try { out.close(); } catch (Exception ignore) {}
            //hash didn't match -> delete file
            if (!success)
                Files.deleteIfExists(local);
        }

        return local;
    }

}

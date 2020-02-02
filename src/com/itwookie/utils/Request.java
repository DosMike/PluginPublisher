package com.itwookie.utils;

import java.io.IOException;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Request {
    private URL url;
    private String requestMethod;

    public Request(String url) throws MalformedURLException {
        this.url = new URL(url);
        this.requestMethod = "GET";
    }

    public Request(String method, String url) throws MalformedURLException {
        this.url = new URL(url);
        this.requestMethod = method;
    }

    public Request(URL url) {
        this.url = url;
        this.requestMethod = "GET";
    }

    public Request(String method, URL url) {
        this.url = url;
        this.requestMethod = method;
    }

    private Map<String, String> requestHeaders = new HashMap<String, String>();

    public void setRequestHeader(String header, String value) {
        requestHeaders.put(header, value);
    }

    private Map<String, String> paramsQuery = new HashMap<String, String>();
    private Map<String, String> paramsBody = new HashMap<String, String>();

    /**
     * Set's a parameter in this Requests Query. The values are automatically URL encoded.
     */
    public void setQueryParameter(String key, String value) {
        paramsQuery.put(key, value);
    }

    /**
     * Set's a parameter in this Requests query. The values are automatically URL encoded.<br>
     * This overload auto converts the value to Strings using the toString()-Method. If value is null, an empty string will be set.
     */
    public void setQueryParameter(String key, Object value) {
        paramsQuery.put(key, value == null ? "" : value.toString());
    }

    /**
     * Set's a parameter in this Requests body. This will automatically create related content headers.
     * The body will be formatted as application/x-www-form-urlencoded data and automatically URL escaped.
     */
    public void setBodyParameter(String key, String value) {
        paramsBody.put(key, value);
    }

    /**
     * Set's a parameter in this Requests body. This will automatically create related content headers.
     * The body will be formatted as application/x-www-form-urlencoded data and automatically URL escaped.<br>
     * This overload auto converts the value to Strings using the toString()-Method. If value is null, an empty string will be set.
     */
    public void setBodyParameter(String key, Object value) {
        paramsQuery.put(key, value == null ? "" : value.toString());
    }

    /**
     * Automatically set the parameter in query or body based on the request method.
     * For POST and PUT requests the body will be used, query otherwise.<br>
     * See setQueryParameter and setBodyParameter for details.
     */
    public void setParameter(String key, String value) {
        if (requestMethod == null || requestMethod.isEmpty())
            throw new IllegalStateException("Request Method was not yet set, but is required for setParameter");

        if ("POST".equalsIgnoreCase(requestMethod) || "PUT".equalsIgnoreCase(requestMethod))
            setBodyParameter(key, value);
        else
            setQueryParameter(key, value);
    }

    /**
     * Automatically set the parameter in query or body based on the request method.
     * For POST and PUT requests the body will be used, query otherwise.<br>
     * See setQueryParameter and setBodyParameter for details.<br>
     * This overload auto converts the value to Strings using the toString()-Method. If value is null, an empty string will be set.
     */
    public void setParameter(String key, Object value) {
        if (requestMethod == null || requestMethod.isEmpty())
            throw new IllegalStateException("Request Method was not yet set, but is required for setParameter");

        if ("POST".equalsIgnoreCase(requestMethod) || "PUT".equalsIgnoreCase(requestMethod))
            setBodyParameter(key, value == null ? "" : value.toString());
        else
            setQueryParameter(key, value == null ? "" : value.toString());
    }

    /**
     * get a http/https URL connection with all data represented by this request set, but nothing more.
     * e.g. timeouts are untouched and setDoInput have to be called if necessary
     */
    public URLConnection getRawConnection() throws IOException, URISyntaxException {
        URL iurl;
        if (!paramsQuery.isEmpty()) { //params can't be null
            //squish query params
            String oldParams = url.getQuery();
            String params = squishAndEncodeParameters(paramsQuery);
            if (oldParams != null)
                params = params + '&' + oldParams;
            //rebuild url with params
            iurl = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), params, url.getRef()).toURL();
        } else {
            iurl = url;
        }
        HttpURLConnection connection = (HttpURLConnection) iurl.openConnection();
        connection.setRequestMethod(requestMethod);
        requestHeaders.put("User-Agent", "HTTP Request Library/1.0 (Java/10) by DosMike");
        if (!paramsBody.isEmpty()) {
            requestHeaders.put("Content-Type", "application/x-www-form-urlencoded");
            requestHeaders.put("Content-Length", String.valueOf(squishAndEncodeParameters(paramsBody).getBytes().length));
            connection.setDoOutput(true);
        }
        for (Map.Entry<String, String> header : requestHeaders.entrySet())
            connection.setRequestProperty(header.getKey(), header.getValue());
        return connection;
    }

    /**
     * this will open the connection if not already opened!
     */
    public void writeRequestBody(HttpURLConnection connection) throws IOException {
        if (!paramsBody.isEmpty())
            connection.getOutputStream().write(getBodyParameterURLencoded().getBytes());
        connection.getOutputStream().flush();
    }

    /**
     * @return one string containing query params or null
     */
    public String getQueryParameterURLencoded() {
        return squishAndEncodeParameters(paramsQuery);
    }

    /**
     * @return one string containing body params or null
     */
    public String getBodyParameterURLencoded() {
        return squishAndEncodeParameters(paramsBody);
    }

    private String squishAndEncodeParameters(Map<String, String> target) {
        if (!target.isEmpty()) {
            StringBuffer sb = new StringBuffer(1024);
            for (Map.Entry<String, String> data : target.entrySet()) {
                if (sb.length() == 0) {
                    sb.append(URLEncoder.encode(data.getKey()));
                    sb.append('=');
                    sb.append(URLEncoder.encode(data.getValue()));
                } else {
                    sb.append('&');
                    sb.append(URLEncoder.encode(data.getKey()));
                    sb.append('=');
                    sb.append(URLEncoder.encode(data.getValue()));
                }
            }
            return sb.toString();
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Request request = (Request) o;
        return Objects.equals(url, request.url) &&
                Objects.equals(requestMethod, request.requestMethod) &&
                Objects.equals(requestHeaders, request.requestHeaders) &&
                Objects.equals(paramsQuery, request.paramsQuery) &&
                Objects.equals(paramsBody, request.paramsBody);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, requestMethod, requestHeaders, paramsQuery, paramsBody);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(requestMethod);
        sb.append(' ');
        sb.append(url.toString());

        sb.append("\nHeader: \n");
        for (Map.Entry<String, String> headers : requestHeaders.entrySet()) {
            sb.append(headers.getKey());
            sb.append(": ");
            sb.append(headers.getValue());
            sb.append("\n");
        }

        sb.append("Query Params: \n");
        {
            String p = getQueryParameterURLencoded();
            if (p != null)
                sb.append(String.join("\n", getQueryParameterURLencoded().split("&")));
        }
        {
            String p = getBodyParameterURLencoded();
            if (p != null) {
                sb.append("Body Params: \n");
                sb.append(String.join("\n", getQueryParameterURLencoded().split("&")));
            } else {
                sb.append("Body Params: ");
            }
        }

        return sb.toString();
    }
}

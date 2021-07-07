package com.zqykj.tldw.aggregate.searching.esclientrhl.util;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext;
import java.io.UnsupportedEncodingException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;

/**
 *  http client tools
 **/
public class HttpClientTool {
    private static HttpClient mHttpClient = null;

    private static CloseableHttpClient getHttpClient(HttpClientBuilder httpClientBuilder) {
        RegistryBuilder<ConnectionSocketFactory> registryBuilder = RegistryBuilder.<ConnectionSocketFactory>create();
        ConnectionSocketFactory plainSF = new PlainConnectionSocketFactory();
        registryBuilder.register("http", plainSF);
        // Specify the trust key store object and connection socket factory
        try {
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            // Trust any link
            TrustStrategy anyTrustStrategy = new TrustStrategy() {
                @Override
                public boolean isTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                    return true;
                }
            };
            SSLContext sslContext = SSLContexts.custom().useTLS().loadTrustMaterial(trustStore, anyTrustStrategy).build();
            LayeredConnectionSocketFactory sslSF = new SSLConnectionSocketFactory(sslContext, SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            registryBuilder.register("https", sslSF);
        } catch (KeyStoreException e) {
            throw new RuntimeException(e);
        } catch (KeyManagementException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        Registry<ConnectionSocketFactory> registry = registryBuilder.build();
        // Set up connection manager
        PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager(registry);
        // Build client
        return httpClientBuilder.setConnectionManager(connManager).build();
    }

    private synchronized static HttpClient getESHttpClient() {
        if (mHttpClient == null) {
//            HttpParams params = new BasicHttpParams();
//            //设置基本参数
//            HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
//            HttpProtocolParams.setContentCharset(params, Constants.CHARSET);
//            HttpProtocolParams.setUseExpectContinue(params, true);
//            //超时设置
//            /*从连接池中取连接的超时时间*/
//            ConnManagerParams.setTimeout(params, Constants.CONMANTIMEOUT);
//            /*连接超时*/
//            HttpConnectionParams.setConnectionTimeout(params, Constants.CONTIMEOUT);
//            /*请求超时*/
//            HttpConnectionParams.setSoTimeout(params, Constants.SOTIMEOUT);
//            //设置HttpClient支持HTTp和HTTPS两种模式
//            SchemeRegistry schReg = new SchemeRegistry();
//            schReg.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
//            schReg.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
//            PoolingClientConnectionManager cm = new PoolingClientConnectionManager(schReg);
//            cm.setMaxTotal(Constants.MAXTOTAL);
//            cm.setDefaultMaxPerRoute(Constants.DEFAULTMAXPERROUTE);
//            mHttpClient = new DefaultHttpClient(cm,params);
            mHttpClient = getHttpClient(HttpClientBuilder.create());
        }
        return mHttpClient;
    }

    /**
     * Get elasticsearch http client
     * @param username: the client username default is elasticsearch
     * @param password: password
     * @return: org.apache.http.client.HttpClient
     **/
    private synchronized static HttpClient getESHttpClient(String username,String password){
        if(mHttpClient == null){
            mHttpClient = getHttpClientWithBasicAuth(username, password);
        }
        return mHttpClient;
    }

    /**
     * @param username: the client username default is elasticsearch
     * @param password: password
     * @return: org.apache.http.impl.client.HttpClientBuilder
     **/
    private static HttpClientBuilder credential(String username, String password) {
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        CredentialsProvider provider = new BasicCredentialsProvider();
        AuthScope scope = new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT, AuthScope.ANY_REALM);
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(username, password);
        provider.setCredentials(scope, credentials);
        httpClientBuilder.setDefaultCredentialsProvider(provider);
        return httpClientBuilder;
    }

    /**
     * Getting httpclient supporting basic auth authentication
     * @param username
     * @param password
     * @return
     */
    private static CloseableHttpClient getHttpClientWithBasicAuth(String username, String password){
        return getHttpClient(credential(username, password));
    }

    /**
     * Set header information, e.g. content type, etc
     * @param req:
     * @param headers:
     * @return: void
     **/
    private static void setHeaders(HttpRequestBase req, Map<String, String> headers){
        if(headers == null){
            return;
        }
        for(Map.Entry<String, String> header : headers.entrySet()){
            req.setHeader(header.getKey(), header.getValue());
        }
    }



    /**
     * Execute HTTP request
     *
     * @param url
     * @param obj
     * @return
     * @throws Exception
     */
    public static String execute(String url, String obj) throws Exception {
        HttpClient httpClient = null;
        HttpResponse response = null;
        httpClient = HttpClientTool.getESHttpClient();
        HttpUriRequest request = postMethod(url, obj);
        response = httpClient.execute(request);
        HttpEntity entity1 = response.getEntity();
        String respContent = EntityUtils.toString(entity1, HTTP.UTF_8).trim();
        return respContent;
    }

    /**
     * Execute HTTP request
     *
     * @param url
     * @param obj
     * @return
     * @throws Exception
     */
    public static String execute(String url, String obj, String username, String password) throws Exception {
        HttpClient httpClient = null;
        HttpResponse response = null;
        httpClient = HttpClientTool.getESHttpClient(username, password);
        HttpUriRequest request = postMethod(url, obj);
        response = httpClient.execute(request);
        HttpEntity entity1 = response.getEntity();
        String respContent = EntityUtils.toString(entity1, HTTP.UTF_8).trim();
        return respContent;
    }

    private static HttpUriRequest postMethod(String url, String data)
            throws UnsupportedEncodingException {
        HttpPost httpPost = new HttpPost(url);
        if (data != null) {
            httpPost.setEntity(new StringEntity(data, "UTF-8"));
        }
        httpPost.addHeader("Content-Type", "application/json");
        return httpPost;
    }

//    static class Constants {
//        /** 编码*/
//        public static final String CHARSET = HTTP.UTF_8;
//        /*从连接池中取连接的超时时间*/
//        public static final int CONMANTIMEOUT = 2000;
//        /*连接超时*/
//        public static final int CONTIMEOUT = 2000;
//        /*请求超时*/
//        public static final int SOTIMEOUT = 5000;
//        /*设置整个连接池最大连接数*/
//        public static final int MAXTOTAL = 6;
//        /*根据连接到的主机对MaxTotal的一个细分*/
//        public static final int DEFAULTMAXPERROUTE = 3;
//    }
}

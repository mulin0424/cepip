// (c) 2009-2011 Miaoxin Li
// This file is distributed as part of the KGG source code package
// and may not be redistributed in any form, without prior written
// permission from the author. Permission is granted for you to
// modify this file for your own personal use, but modified versions
// must retain this copyright notice and must not be distributed.
// Permission is granted for you to use this file to compile IGG.
// All computer programs have bugs. Use this file at your own risk.
// Tuesday, March 01, 2011
package org.cobi.util.download.stable;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.util.EntityUtils;
 
/**
 *
 * @author MX Li
 */
public class HttpClient4API {

    public static long getContentLength(String curUrl) throws IOException, ClientProtocolException, Exception {
        HttpClient curHttpClient = new DefaultHttpClient();
        HttpHead httpHead = new HttpHead(curUrl);
        try {
            HttpResponse response = curHttpClient.execute(httpHead);
            long length = -1;
            //��ȡHTTP״̬��
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200) {
                throw new Exception("Failed to connect " + curUrl);
            }
            /*
             if (getDebug()) {
             for (Header header : response.getAllHeaders()) {
             System.out.println(header.getName() + ":" + header.getValue());
             }
             }
             *
             */

            //Content-Length
            Header[] headers = response.getHeaders("Content-Length");
            if (headers.length > 0) {
                length = Long.valueOf(headers[0].getValue());
            }
            return length;
        } catch (Exception e) {
            throw e;
        } finally {
            httpHead.abort();
            curHttpClient.getConnectionManager().shutdown();
        }
    }

    public static String getContent(String curUrl) throws IOException, ClientProtocolException, Exception {
        String content = null;
        HttpClient curHttpClient = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(curUrl);

        try {
            HttpResponse response = curHttpClient.execute(httpGet);

            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                //  throw new Exception("��Դ������!");
                return null;
            }

            HttpEntity entity = response.getEntity();
            if (entity != null) {
                content = EntityUtils.toString(entity, "utf-8");
                //System.out.println(content);
            }

            response.getEntity().consumeContent();
        } catch (Exception e) {
            throw new Exception(e.toString() + " in " + curUrl);
        } finally {
            curHttpClient.getConnectionManager().shutdown();
        }
        return content;
    }

    public static boolean checkConnection(String url) throws Exception {
        HttpClient curHttpClient = new DefaultHttpClient();
        HttpHead httpHead = new HttpHead(url);
        curHttpClient.getParams().setIntParameter("httpclient.socket.timeout", 100);
        DefaultHttpRequestRetryHandler retryHandler = new DefaultHttpRequestRetryHandler(0, false);
        ((AbstractHttpClient) curHttpClient).setHttpRequestRetryHandler(retryHandler);
        try {
            HttpResponse response = curHttpClient.execute(httpHead);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200) {
                return false;
            }
        } catch (Exception ex) {
            return false;
        } finally {
            httpHead.abort();
            // When HttpClient instance is no longer needed,
            // shut down the connection manager to ensure
            // immediate deallocation of all system resources
            curHttpClient.getConnectionManager().shutdown();
        }
        return true;
    }
 
     
    public static void simpleRetriever(String url, String outPath) throws Exception {
        //   http://www.genenames.org/cgi-bin/hgnc_downloads.cgi?title=HGNC+output+data&hgnc_dbtag=on&col=gd_hgnc_id&col=gd_app_sym&col=gd_app_name&col=gd_status&col=gd_prev_sym&col=gd_aliases&col=gd_pub_chrom_map&col=gd_pub_acc_ids&col=gd_pub_refseq_ids&status=Approved&status=Entry+Withdrawn&status_opt=2&where=&order_by=gd_pub_chrom_map_sort&format=text&limit=&submit=submit&.cgifields=&.cgifields=chr&.cgifields=status&.cgifields=hgnc_dbtag

        HttpClient httpClient = new DefaultHttpClient();
        try {
            HttpGet httpGet = new HttpGet(url);

            HttpResponse response = httpClient.execute(httpGet);
            int statusCode = response.getStatusLine().getStatusCode();
            if (HttpClient4DownloadTask.getDebug()) {
                for (Header header : response.getAllHeaders()) {
                    System.out.println(header.getName() + ":" + header.getValue());
                }
                System.out.println("statusCode:" + statusCode);
            }
            if (statusCode == 206 || (statusCode == 200)) {
                InputStream inputStream = response.getEntity().getContent();
                // BufferedInputStream bis = new BufferedInputStream(is, temp.length);
                //��������д��
                RandomAccessFile outputStream = new RandomAccessFile(outPath, "rw");
                //��ָ��λ��

                int count = 0;
                byte[] buffer = new byte[10 * 1024];
                while ((count = inputStream.read(buffer, 0, buffer.length)) > 0) {
                    outputStream.write(buffer, 0, count);
                }
                outputStream.close();
                response.getEntity().consumeContent();
            }
        } finally {
            httpClient.getConnectionManager().shutdown();
        }
    }
}

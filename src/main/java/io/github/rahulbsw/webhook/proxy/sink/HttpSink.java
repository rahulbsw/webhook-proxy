package io.github.rahulbsw.webhook.proxy.sink;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import io.github.rahulbsw.webhook.proxy.Message;
import io.github.rahulbsw.webhook.proxy.Transformer;
import io.github.rahulbsw.webhook.proxy.sink.exception.SinkFailureException;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.Objects;

public class HttpSink extends AbstractSink<String,String> {

    final HttpClient client;
    final String  url;
    final boolean doPost;

    protected HttpSink(Map<String, Object> param, Transformer transformer) {
        super(param,transformer);
        this.client=new HttpClient();
        if(Objects.nonNull(param) && Objects.nonNull(param.get("url"))) {
            this.url=param.get("url").toString();
            this.doPost = true;
        }
        else {
            this.url = null;
            this.doPost = false;
        }

    }

    @Override
    public Message<String,String> post(JsonNode value) throws SinkFailureException {
        if(doPost) {
            try {
                    client.post(url,value.toString());
            } catch (Exception e) {
                throw new SinkFailureException(e);
            }
        }
        return new Message.Builder<String,String>().put("Message","Successfully posted").build();
    }

    public static class HttpClient {
        public final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

        OkHttpClient client = new OkHttpClient();

        String post(String url, String json) throws Exception {
            RequestBody body = RequestBody.create(json, JSON); // new
            // RequestBody body = RequestBody.create(JSON, json); // old
            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .build();
            Response response=null;
             try {
                 response = client.newCall(request).execute();
                 String message=response.body().string();
                 response.body().close();
                 return message;
            }catch (Exception e){
                throw e;
            }finally {
                if(Objects.nonNull(response))
                     response.body().close();
             }


        }
    }
}

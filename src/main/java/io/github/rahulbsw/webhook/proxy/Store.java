package io.github.rahulbsw.webhook.proxy;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.rahulbsw.webhook.proxy.sink.exception.SinkFailureException;
import io.github.rahulbsw.webhook.proxy.sink.AbstractSink;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static io.github.rahulbsw.webhook.proxy.sink.SinkFactory.getSink;

public class Store {
    private static DB db;
    private static ConcurrentMap<String,WebhookConfig> map;

    private static ConcurrentMap<String, AbstractSink> sinks=new ConcurrentHashMap<>();
    private static void createDB(String dbFile){
        db= DBMaker
                .fileDB(dbFile)
                .fileMmapEnable()
                .closeOnJvmShutdown()
                .checksumHeaderBypass()
                .make();
        map = db
                .hashMap("map", Serializer.STRING,Serializer.JAVA)
                .createOrOpen();

        //Runtime.getRuntime().addShutdownHook(new Thread(() -> Store.close()));
    }

    public static void init(String dbFile){
        if(Objects.isNull(db)||db.isClosed()) {
            createDB(dbFile);
        }
        if(map.size()>0)
        {
            for (Map.Entry<String,WebhookConfig> entry: map.entrySet()) {
                sinks.put(entry.getKey(),getSink(entry.getValue()));
            }
        }
    }



    public static void close(){
        if(Objects.isNull(db)||db.isClosed()) {
           db.close();
           map=null;
           db=null;
        }
    }



    public static WebhookConfig get(String key) {
        return map.get(key);
    }

    @Nullable
    public static WebhookConfig put(String key, WebhookConfig value) {
        sinks.put(key,getSink(value));
        return map.put(key, value);

    }

    @Nullable
    public static WebhookConfig put(WebhookConfig webhookConfig) {
        sinks.put(webhookConfig.name,getSink(webhookConfig));
        return map.put(webhookConfig.name, webhookConfig);

    }

    public static WebhookConfig remove(Object key) {
        sinks.remove(key);
        return map.remove(key);
    }

    public static void putAll(@NotNull Map<? extends String, ? extends WebhookConfig> m) {
        map.putAll(m);
        init(null);
    }

    public static Message process(String name,JsonNode message) throws SinkFailureException {
         return sinks.get(name).process(message);
    }


}

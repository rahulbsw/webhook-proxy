package io.github.rahulbsw.webhook.proxy.sink;

import io.github.rahulbsw.webhook.proxy.WebhookConfig;
import io.github.rahulbsw.webhook.proxy.Transformer;

import java.util.Objects;

public class SinkFactory
{
    public enum SinkType {HTTP,CONSOLE,NULL,KAFKA,MONGODB}
    public static AbstractSink getSink(WebhookConfig webhookConfig){
        Transformer transformer;
        SinkType sinkType;
        if(Objects.isNull(webhookConfig.sink)||Objects.isNull(SinkType.valueOf(webhookConfig.sink))||SinkType.CONSOLE.equals(SinkType.valueOf(webhookConfig.sink))){
            sinkType=SinkType.CONSOLE;
        } else {
            sinkType=SinkType.valueOf(webhookConfig.sink);
        }
        transformer=new Transformer(webhookConfig.spec);
        switch (sinkType){
            case HTTP: return new HttpSink(webhookConfig.config,transformer);
            case KAFKA: return new KafkaSink(webhookConfig.config,transformer);
            case MONGODB: return new MongodbSink(webhookConfig.config,transformer);
            case NULL:return new NullSink(webhookConfig.config,transformer);
            default: return new ConsoleSink(webhookConfig.config,transformer);
        }

    }
}

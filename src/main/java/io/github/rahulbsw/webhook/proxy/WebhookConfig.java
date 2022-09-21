package io.github.rahulbsw.webhook.proxy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import io.github.rahulbsw.webhook.proxy.sink.SinkFactory;

import java.io.Serializable;
import java.util.Map;


public class WebhookConfig implements Serializable {
    public final String name;
    public Map<String, Object> config;
    public final String spec;

    public final String sink;

    public WebhookConfig(Map<String, Object> config, String spec, String name, String sink)  {
        this.config = config;
        this.spec = spec;
        this.name=name;
        this.sink=(Strings.isNullOrEmpty(sink))? SinkFactory.SinkType.CONSOLE.name() :sink.toUpperCase();
    }
}

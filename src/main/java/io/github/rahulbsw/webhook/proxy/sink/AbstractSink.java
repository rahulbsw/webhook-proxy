package io.github.rahulbsw.webhook.proxy.sink;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.rahulbsw.webhook.proxy.Message;
import io.github.rahulbsw.webhook.proxy.Transformer;
import io.github.rahulbsw.webhook.proxy.sink.exception.SinkFailureException;

import java.util.Map;
import java.util.Objects;

abstract public class AbstractSink<K,V> {
    final Transformer transformer;
    protected AbstractSink(Map<String, Object> param, Transformer transformer){
        this.transformer=transformer;
    }

    public Message<K,V> process(JsonNode input) throws SinkFailureException {
        JsonNode value= transformer.transform(input);
        return post(value);
    }

    abstract public Message<K,V> post(JsonNode value) throws SinkFailureException;
}

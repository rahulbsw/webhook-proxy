package io.github.rahulbsw.webhook.proxy.sink;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import io.github.rahulbsw.webhook.proxy.Message;
import io.github.rahulbsw.webhook.proxy.Transformer;
import io.github.rahulbsw.webhook.proxy.sink.exception.SinkFailureException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

abstract public class AbstractSink<K,V> {
    final Transformer transformer;
    final boolean isMessageList;
    protected AbstractSink(Map<String, Object> param, Transformer transformer){
        this.transformer=transformer;
        this.isMessageList=Boolean.parseBoolean(param.getOrDefault("isMessageList","true").toString());
    }

    public List<Message<K,V>> process(JsonNode input) throws SinkFailureException {
        JsonNode value= transformer.transform(input);
        if(!value.isEmpty()){
            if (isMessageList && value.isArray()) {
                List<Message<K, V>> messages = new ArrayList<>();
                for (JsonNode node : ((ArrayNode) value)) {
                    if (!node.isEmpty())
                        messages.add(post(node));
                }
                return messages;
            } else {
                return Arrays.asList(post(value));
            }
        }
        return new ArrayList<>();
    }

    abstract public Message<K,V> post(JsonNode value) throws SinkFailureException;
}

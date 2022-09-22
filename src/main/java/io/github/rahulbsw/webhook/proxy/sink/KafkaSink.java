package io.github.rahulbsw.webhook.proxy.sink;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.rahulbsw.webhook.proxy.Message;
import io.github.rahulbsw.webhook.proxy.Transformer;
import io.github.rahulbsw.webhook.proxy.sink.exception.SinkFailureException;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Map;

public class KafkaSink extends AbstractSink{
    public static final String KEY_KEY="key";
    public static final String VALUE_KEY="value";
    public static final String KAFKA_PRODUCER_KEY="KafkaProducer";
    public static final String TOPIC_NAME_KEY="TopicName";
    final KafkaProducer producer;
    final String topicName;
    protected KafkaSink(Map param, Transformer transformer) {
        super(param, transformer);
        this.producer=(KafkaProducer)param.get(KAFKA_PRODUCER_KEY);
        this.topicName=param.get(TOPIC_NAME_KEY).toString();
    }



    @Override
    public Message<String,String> post(JsonNode value) throws SinkFailureException {
        ProducerRecord<String,String> record;
        if(value.has(KEY_KEY)  && value.has(VALUE_KEY))
            record=new ProducerRecord<>(topicName,value.get(KEY_KEY).toString(),value.get(VALUE_KEY).toString());
        else
            record=new ProducerRecord<>(topicName,value.toString());
        producer.send(record);
        return new Message.Builder<String,String>().put("Message","Successfully posted").build();
    }
}

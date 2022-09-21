package io.github.rahulbsw.webhook.proxy;

import java.util.HashMap;

public class Message<K,V> extends HashMap<K,V> {

    public static class Builder<K,V>{
        private Message<K,V> message;
        public Builder()
        {
            message=new Message<>();
        }
        public Message<K,V> build(){
           return message;
        }

        public Builder<K,V> put(K key, V value){
            message.put(key,value);
            return this;
        }

    }
}

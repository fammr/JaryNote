/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package note.kafka.v10.low;

import note.kafka.KafkaProperties;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.util.Date;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.Future;

public class Producer {
    private final KafkaProducer<String, String> producer;
    private final String topic;

    public Producer(String topic) {
        Properties props = new Properties();
        props.put("bootstrap.servers", KafkaProperties.BOOTSTRAP_SERVERS);
        props.put("client.id", "DemoProducer");

        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		props.put("acks", "all");
		props.put("retries", 3);
		props.put("batch.size", 16384);//16M
		//props.put("linger.ms", 1000); // it will failed when set this to 10000.
		props.put("buffer.memory", 33554432);//32M
		props.put("partitioner.class", "note.kafka.v10.low.SimplePartitioner");

        producer = new KafkaProducer<>(props);
		System.out.println(producer.partitionsFor(KafkaProperties.TOPIC)
				+"\n"+ producer.metrics().toString());

		this.topic = topic;
    }

    public void producerMsg() throws InterruptedException{
    	int events = Integer.MAX_VALUE;
    	for (int  nEvents = 0; nEvents < events; nEvents++) {
			try {
				Thread.sleep(0);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
	    	long runtime = new Date().getTime();
	        String key = String.valueOf(nEvents);

	        int partition=1;
	        if(Integer.parseInt(key)%2==0){
	        	partition=0;
	        }
	        String value = key +"="+ runtime;
	        try {
	            //producer.send(new ProducerRecord<>(topic,partition,key, value));
				System.out.println("Sent message: (" + key + ", " + value + ")");
				//Future<RecordMetadata> a = producer.send(new ProducerRecord<>(topic,partition,key, value));

				Future<RecordMetadata> a = producer.send(new ProducerRecord<>(topic,key, value)); // overwrite partitioner
				Thread.sleep(500);
				System.out.println(a.isDone() + ", topic=" + a.get().topic()+" , partition="+a.get().partition()+", offset="+ a.get().offset());
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
    	}
    	Thread.sleep(10000);
    }
    
    public static void main(String[] args) throws InterruptedException {
    	Producer producer = new Producer(KafkaProperties.TOPIC);
    	producer.producerMsg();
	}
}


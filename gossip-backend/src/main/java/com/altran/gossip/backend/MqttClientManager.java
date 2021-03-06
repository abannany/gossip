package com.altran.gossip.backend;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.altran.gossip.backend.config.MqttConfig;
import com.altran.gossip.backend.dao.DaoListener;

import io.dropwizard.lifecycle.Managed;

/**
 * MQTT client manager that subscribes to the gossipeous topics.
 *
 */
public class MqttClientManager implements Managed {
    private static final Logger LOG = LoggerFactory.getLogger(MqttClientManager.class);

    private MqttClient client;
    private final MqttConnectOptions options = new MqttConnectOptions();;

    public MqttClientManager(MqttConfig mqttConfig, DaoListener dao) throws MqttException {
        String userName = mqttConfig.getUserName();
        String password = mqttConfig.getPassword();
        client = new MqttClient(mqttConfig.getUrl(), MqttClient.generateClientId());

        client.setCallback(new NewGossipCallback(this, dao));
        options.setConnectionTimeout(60);
        options.setKeepAliveInterval(60);
        options.setUserName(userName);
        options.setPassword(password.toCharArray());
    }

    /**
     * Publish something back to clients
     * 
     * @param topic The command topic {CMD, CONFIG, MEASURE}
     * @throws MqttPersistenceException
     * @throws MqttException
     */
    public void publish(String topic, MqttMessage message) throws MqttPersistenceException, MqttException {
        client.publish(topic, message);
    }

    @Override
    public void start() throws Exception {
        LOG.info("Connecting mqtt broker with options: {}", options);;
        client.connect(options);
        client.subscribe("#", 0);
    }

    @Override
    public void stop() throws Exception {
        LOG.info("Closing mqtt connection...");
        client.disconnect();
    }

    public MqttClient getClient() {
        return client;
    }
}

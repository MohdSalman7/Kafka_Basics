package com.github.TwitterKafkaProject;

import com.google.common.collect.Lists;
import com.twitter.hbc.ClientBuilder;
import com.twitter.hbc.core.Client;
import com.twitter.hbc.core.Constants;
import com.twitter.hbc.core.Hosts;
import com.twitter.hbc.core.HttpHosts;
import com.twitter.hbc.core.endpoint.StatusesFilterEndpoint;
import com.twitter.hbc.core.processor.StringDelimitedProcessor;
import com.twitter.hbc.httpclient.auth.Authentication;
import com.twitter.hbc.httpclient.auth.OAuth1;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class TwitterProducer {

    String consumerKey = "UpaJWZlDB2Dsp3Rc3cVNoJzVI";
    String consumerSecret = "IZlvpfnM8yLj5KooLe5mHJndfjUQJcWDqT6uH6ymO9UyArmATd";
    String token = "1448987889593847814-JZMhYsNtAaT0aSuEmEQsjdamNzkAjG";
    String secret = "UDAukq9Et5N541DYciRCi0RKwfG1NWlnfJiYeWsD7q0YJ";

    Logger logger = LoggerFactory.getLogger(TwitterProducer.class.getName());

    public  TwitterProducer(){}

    public static void main(String[] args) {
        new TwitterProducer().run();
    }

    public  void  run()
    {
        logger.info("Setup");
        /** Set up your blocking queues: Be sure to size these properly based on expected TPS of your stream */
        BlockingQueue<String> msgQueue = new LinkedBlockingQueue<String>(100000);

        // create a twitter client
        Client client = createTwitterClient(msgQueue);
        // Attempts to establish a connection
        client.connect();

        // create a kafka producer

        // loop to send tweets to kafka
        while (!client.isDone())
        {
            String msg = null;
            try {
                msg= msgQueue.poll(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
                client.stop();
            }
            if(msg!= null)
            {
                logger.info(msg);
            }
        }
        logger.info("End of application");
    }

    public Client createTwitterClient(BlockingQueue<String> msgQueue)
    {

        /** Declare the host you want to connect to, the endpoint, and authentication (basic auth or oauth) */
        Hosts hosebirdHosts = new HttpHosts(Constants.STREAM_HOST);
        StatusesFilterEndpoint hosebirdEndpoint = new StatusesFilterEndpoint();
        // Optional: set up some followings and track terms

        List<String> terms = Lists.newArrayList("bitcoin");
        hosebirdEndpoint.trackTerms(terms);

        // These secrets should be read from a config file
        Authentication hosebirdAuth = new OAuth1(consumerKey, consumerSecret, token, secret);

        ClientBuilder builder = new ClientBuilder()
                .name("Hosebird-Client-01")                              // optional: mainly for the logs
                .hosts(hosebirdHosts)
                .authentication(hosebirdAuth)
                .endpoint(hosebirdEndpoint)
                .processor(new StringDelimitedProcessor(msgQueue));

                 Client hosebirdClient = builder.build();
                 return hosebirdClient;

    }

}

package com.java.fernando.nflpickem.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;

@Configuration
public class MongoConfig extends AbstractMongoClientConfiguration {

    @Override
    protected String getDatabaseName() {
        return "NFLPickEm";
    }

    @Override
    public MongoClient mongoClient() {
        return MongoClients.create("mongodb+srv://fiap:JAVADEV44@cluster0.uxooxid.mongodb.net/NFLPickEm?authSource=admin&retryWrites=true&w=majority");
    }

    @Override
    protected boolean autoIndexCreation() {
        return true;
    }
}
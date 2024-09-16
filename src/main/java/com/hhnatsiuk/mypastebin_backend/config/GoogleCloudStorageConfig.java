package com.hhnatsiuk.mypastebin_backend.config;

import org.springframework.context.annotation.Bean;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class GoogleCloudStorageConfig {


    @Bean
    public Storage googleCloudStorage() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();

        return StorageOptions.newBuilder()
                .setCredentials(GoogleCredentials.fromStream(
                        classLoader.getResourceAsStream("credentials.json")))
                .build()
                .getService();

    }
}

package com.eip.document.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;

@Configuration
public class GcsConfig {

    @Value("${document.gcs.project-id}")
    private String projectId;

    @Value("${document.gcs.credentials-path:#{null}}")
    private String credentialsPath;

    @Bean
    public Storage gcsStorage() throws IOException {
        StorageOptions.Builder builder = StorageOptions.newBuilder().setProjectId(projectId);

        if (credentialsPath != null && !credentialsPath.isBlank()) {
            GoogleCredentials credentials = GoogleCredentials.fromStream(
                    new FileInputStream(credentialsPath));
            builder.setCredentials(credentials);
        } else {
            // Use Application Default Credentials (for GKE/Cloud Run)
            builder.setCredentials(GoogleCredentials.getApplicationDefault());
        }

        return builder.build().getService();
    }
}

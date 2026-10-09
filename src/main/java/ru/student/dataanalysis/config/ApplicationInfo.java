package ru.student.dataanalysis.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationInfo {

    @Value("${app.name}")
    private String name;

    @Value("${app.version}")
    private String version;

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }
}

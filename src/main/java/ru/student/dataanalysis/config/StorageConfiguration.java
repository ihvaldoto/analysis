package ru.student.dataanalysis.config;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import ru.student.dataanalysis.model.DataPoint;
import ru.student.dataanalysis.model.DataSet;
import ru.student.dataanalysis.model.User;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class StorageConfiguration {

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    public List<User> userStorage() {
        return new ArrayList<>();
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    public List<DataSet> dataSetStorage() {
        return new ArrayList<>();
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    public List<DataPoint> dataPointStorage() {
        return new ArrayList<>();
    }
}

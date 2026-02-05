package com.silverio.tasks.shared.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.Locale;

@Configuration
public class LocaleConfig {

  @Bean
  public ResourceBundleMessageSource messageSource() {
    var ms = new ResourceBundleMessageSource();
    ms.setBasename("messages");
    ms.setDefaultEncoding("UTF-8");
    ms.setDefaultLocale(new Locale("pt", "BR"));
    return ms;
  }

  @Bean
  public LocalValidatorFactoryBean getValidator(ResourceBundleMessageSource messageSource) {
    var bean = new LocalValidatorFactoryBean();
    bean.setValidationMessageSource(messageSource);
    return bean;
  }
}

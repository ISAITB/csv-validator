package eu.europa.ec.itb.csv.config;

import eu.europa.ec.itb.csv.DomainConfig;
import eu.europa.ec.itb.validation.commons.config.DomainPluginConfigProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Define common spring beans for the validator.
 */
@Configuration
public class BeanConfiguration {

    /**
     * Enable custom plugin support using the default plugin provider.
     *
     * @return The bean.
     */
    @Bean
    public DomainPluginConfigProvider<DomainConfig> pluginConfigProvider() {
        return new DomainPluginConfigProvider<>();
    }

}

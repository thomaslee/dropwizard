package io.dropwizard.testing.junit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.servlet.ServletProperties;
import org.glassfish.jersey.test.DeploymentContext;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.ServletDeploymentContext;
import org.glassfish.jersey.test.spi.TestContainerFactory;

import javax.validation.Validator;
import javax.ws.rs.client.Client;
import java.util.function.Consumer;

public class ResourceTestHelper implements AutoCloseable {
    private final ResourceTestJerseyConfiguration configuration;
    private JerseyTest test;


    /**
     * A {@link ResourceTestHelper} builder which enables configuration of a Jersey testing environment.
     */
    public static class Builder {

        private final ResourceTestJerseyConfiguration.Builder builder = ResourceTestJerseyConfiguration.builder();

        public Builder setMapper(ObjectMapper mapper) {
            builder.setMapper(mapper);
            return this;
        }

        public Builder setValidator(Validator validator) {
            builder.setValidator(validator);
            return this;
        }

        public Builder setClientConfigurator(Consumer<ClientConfig> clientConfigurator) {
            builder.setClientConfigurator(clientConfigurator);
            return this;
        }

        public Builder addResource(Object resource) {
            builder.addResource(resource);
            return this;
        }

        public Builder addProvider(Class<?> klass) {
            builder.addProvider(klass);
            return this;
        }

        public Builder addProvider(Object provider) {
            builder.addProvider(provider);
            return this;
        }

        public Builder addProperty(String property, Object value) {
            builder.addProperty(property, value);
            return this;
        }

        public Builder setTestContainerFactory(TestContainerFactory factory) {
            builder.setTestContainerFactory(factory);
            return this;
        }

        public Builder setRegisterDefaultExceptionMappers(boolean value) {
            builder.setRegisterDefaultExceptionMappers(value);
            return this;
        }

        /**
         * Builds a {@link ResourceTestHelper} with a configured Jersey testing environment.
         *
         * @return a new {@link ResourceTestHelper}
         */
        public ResourceTestHelper build() throws Exception {
            return new ResourceTestHelper(builder.build());
        }
    }

    ResourceTestHelper(final ResourceTestJerseyConfiguration configuration) throws Exception {
        this.configuration = configuration;
        DropwizardTestResourceConfig.CONFIGURATION_REGISTRY.put(configuration.getId(), configuration);
        try {
            test = new JerseyTest() {
                @Override
                protected TestContainerFactory getTestContainerFactory() {
                    return configuration.testContainerFactory;
                }

                @Override
                protected DeploymentContext configureDeployment() {
                    return ServletDeploymentContext.builder(new DropwizardTestResourceConfig(configuration))
                        .initParam(ServletProperties.JAXRS_APPLICATION_CLASS,
                            DropwizardTestResourceConfig.class.getName())
                        .initParam(DropwizardTestResourceConfig.CONFIGURATION_ID, configuration.getId())
                        .build();
                }

                @Override
                protected void configureClient(ClientConfig clientConfig) {
                    final JacksonJsonProvider jsonProvider = new JacksonJsonProvider();
                    jsonProvider.setMapper(configuration.mapper);
                    configuration.clientConfigurator.accept(clientConfig);
                    clientConfig.register(jsonProvider);
                }
            };
            test.setUp();
        }
        catch (Exception e) {
            removeConfigurationFromRegistry();
            throw e;
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public Client client() {
        return test.client();
    }

    public JerseyTest getJerseyTest() {
        return test;
    }

    public void close() throws Exception {
        removeConfigurationFromRegistry();
        if (test != null) {
            test.tearDown();
        }
    }

    private void removeConfigurationFromRegistry() {
        DropwizardTestResourceConfig.CONFIGURATION_REGISTRY.remove(configuration.getId());
    }
}

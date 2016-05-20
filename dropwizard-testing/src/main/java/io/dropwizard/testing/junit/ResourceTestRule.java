package io.dropwizard.testing.junit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.jersey.validation.Validators;
import io.dropwizard.logging.BootstrapLogging;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.servlet.ServletProperties;
import org.glassfish.jersey.test.DeploymentContext;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.ServletDeploymentContext;
import org.glassfish.jersey.test.inmemory.InMemoryTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerFactory;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import javax.validation.Validator;
import javax.ws.rs.client.Client;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * A JUnit {@link TestRule} for testing Jersey resources.
 */
public class ResourceTestRule implements TestRule {

    static {
        BootstrapLogging.bootstrap();
    }

    /**
     * A {@link ResourceTestRule} builder which enables configuration of a Jersey testing environment.
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
         * Builds a {@link ResourceTestRule} with a configured Jersey testing environment.
         *
         * @return a new {@link ResourceTestRule}
         */
        public ResourceTestRule build() {
            return new ResourceTestRule(builder.build());
        }
    }

    /**
     * Creates a new Jersey testing environment builder for {@link ResourceTestRule}
     *
     * @return a new {@link Builder}
     */
    public static Builder builder() {
        return new Builder();
    }

    private final ResourceTestJerseyConfiguration configuration;
    private ResourceTestHelper resourceTestHelper;

    private ResourceTestRule(ResourceTestJerseyConfiguration configuration) {
        this.configuration = configuration;
    }

    public Validator getValidator() {
        return configuration.validator;
    }

    public ObjectMapper getObjectMapper() {
        return configuration.mapper;
    }

    public Consumer<ClientConfig> getClientConfigurator() {
        return configuration.clientConfigurator;
    }

    public Client client() {
        return resourceTestHelper.client();
    }

    public JerseyTest getJerseyTest() {
        return resourceTestHelper.getJerseyTest();
    }

    @Override
    public Statement apply(Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                resourceTestHelper = new ResourceTestHelper(configuration);
                try {
                    base.evaluate();
                }
                finally {
                    resourceTestHelper.close();
                }
            }
        };
    }
}

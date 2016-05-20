package io.dropwizard.testing.junit;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.jersey.validation.Validators;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.test.inmemory.InMemoryTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerFactory;

import javax.validation.Validator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * A configuration of a Jersey testing environment.
 * Encapsulates data required to configure a {@link ResourceTestRule}.
 * Primarily accessed via {@link DropwizardTestResourceConfig}.
 */
class ResourceTestJerseyConfiguration {

    final Set<Object> singletons;
    final Set<Class<?>> providers;
    final Map<String, Object> properties;
    final ObjectMapper mapper;
    final Validator validator;
    final Consumer<ClientConfig> clientConfigurator;
    final TestContainerFactory testContainerFactory;
    final boolean registerDefaultExceptionMappers;

    /**
     * A {@link ResourceTestJerseyConfiguration} builder which enables configuration of a Jersey testing environment.
     */
    static class Builder {

        private final Set<Object> singletons = new HashSet<>();
        private final Set<Class<?>> providers = new HashSet<>();
        private final Map<String, Object> properties = new HashMap<>();
        private ObjectMapper mapper = Jackson.newObjectMapper();
        private Validator validator = Validators.newValidator();
        private Consumer<ClientConfig> clientConfigurator = c -> {};
        private TestContainerFactory testContainerFactory = new InMemoryTestContainerFactory();
        private boolean registerDefaultExceptionMappers = true;

        Builder setMapper(ObjectMapper mapper) {
            this.mapper = mapper;
            return this;
        }

        Builder setValidator(Validator validator) {
            this.validator = validator;
            return this;
        }

        Builder setClientConfigurator(Consumer<ClientConfig> clientConfigurator) {
            this.clientConfigurator = clientConfigurator;
            return this;
        }

        Builder addResource(Object resource) {
            singletons.add(resource);
            return this;
        }

        Builder addProvider(Class<?> klass) {
            providers.add(klass);
            return this;
        }

        Builder addProvider(Object provider) {
            singletons.add(provider);
            return this;
        }

        Builder addProperty(String property, Object value) {
            properties.put(property, value);
            return this;
        }

        Builder setTestContainerFactory(TestContainerFactory factory) {
            this.testContainerFactory = factory;
            return this;
        }

        Builder setRegisterDefaultExceptionMappers(boolean value) {
            registerDefaultExceptionMappers = value;
            return this;
        }

        /**
         * Builds a {@link ResourceTestJerseyConfiguration} with a configured Jersey testing environment.
         *
         * @return a new {@link ResourceTestJerseyConfiguration}
         */
        public ResourceTestJerseyConfiguration build() {
            return new ResourceTestJerseyConfiguration(
                singletons, providers, properties, mapper, validator,
                clientConfigurator, testContainerFactory, registerDefaultExceptionMappers);
        }
    }

    ResourceTestJerseyConfiguration(Set<Object> singletons, Set<Class<?>> providers, Map<String, Object> properties,
                                    ObjectMapper mapper, Validator validator, Consumer<ClientConfig> clientConfigurator,
                                    TestContainerFactory testContainerFactory, boolean registerDefaultExceptionMappers) {
        this.singletons = singletons;
        this.providers = providers;
        this.properties = properties;
        this.mapper = mapper;
        this.validator = validator;
        this.clientConfigurator = clientConfigurator;
        this.testContainerFactory = testContainerFactory;
        this.registerDefaultExceptionMappers = registerDefaultExceptionMappers;
    }

    String getId() {
        return String.valueOf(hashCode());
    }


    public static Builder builder() {
        return new Builder();
    }
}

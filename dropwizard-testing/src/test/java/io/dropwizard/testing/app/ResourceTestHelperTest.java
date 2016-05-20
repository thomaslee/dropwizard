package io.dropwizard.testing.app;

import io.dropwizard.testing.junit.ResourceTestHelper;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ResourceTestHelperTest {
    @Test
    public void testResourceTestHelper() throws Exception {
        final ResourceTestHelper resourceTestHelper = ResourceTestHelper.builder()
            .addResource(new ContextInjectionResource())
            .build();
        try {
            assertThat(resourceTestHelper.getJerseyTest().target("test").request().get(String.class)).isEqualTo("test");
        }
        finally {
            resourceTestHelper.close();
        }
    }
}

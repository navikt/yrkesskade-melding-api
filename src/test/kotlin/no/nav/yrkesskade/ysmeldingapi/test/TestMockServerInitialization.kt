package no.nav.yrkesskade.ysmeldingapi.test

import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ApplicationEvent
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.event.ContextClosedEvent
import org.springframework.test.context.support.TestPropertySourceUtils


class TestMockServerInitialization : ApplicationContextInitializer<ConfigurableApplicationContext> {
    override fun initialize(configurableApplicationContext: ConfigurableApplicationContext) {
        val testMockServer = TestMockServer()
        testMockServer.start()
        configurableApplicationContext
            .beanFactory
            .registerSingleton("testMockServer", testMockServer)
        configurableApplicationContext.addApplicationListener { applicationEvent: ApplicationEvent? ->
            if (applicationEvent is ContextClosedEvent) {
                testMockServer.stop()
            }
        }

        TestPropertySourceUtils.addInlinedPropertiesToEnvironment(
            configurableApplicationContext,
            "mock.port=" + testMockServer.port()
        )
    }
}
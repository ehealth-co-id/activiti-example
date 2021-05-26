package id.co.ehealth.bpm.example

import org.activiti.api.process.model.IntegrationContext
import org.activiti.api.process.model.ProcessDefinition
import org.activiti.api.process.model.ProcessInstance
import org.activiti.api.process.model.builders.ProcessPayloadBuilder
import org.activiti.api.process.runtime.ProcessRuntime
import org.activiti.api.process.runtime.connector.Connector
import org.activiti.api.runtime.shared.query.Pageable
import org.activiti.spring.process.ProcessExtensionService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController


@SpringBootApplication
@RestController
class ExampleApplication(@Autowired private val processRuntime: ProcessRuntime,
                         @Autowired private val processExtensionService: ProcessExtensionService) {

    @PostMapping("/documents")
    fun processFile(@RequestBody content: String?): String? {
        val processInstance: ProcessInstance = processRuntime.start(ProcessPayloadBuilder
                .start()
                .withProcessDefinitionKey("categorizeProcess")
                .withVariable("fileContent",
                        content)
                .build())

        println("fileContent: $content")
        val message = ">>> Created Process Instance: $processInstance"
        println(message)
        return message
    }

    @GetMapping("/process-definitions")
    fun getProcessDefinition(): List<ProcessDefinition?>? {
        return processRuntime!!.processDefinitions(Pageable.of(0, 100)).getContent()
    }

    @Bean
    fun processTextConnector(): Connector? {
        return Connector { integrationContext: IntegrationContext ->
            val inBoundVariables = integrationContext.inBoundVariables
            println("inBoundVariables: $inBoundVariables")
            val contentToProcess = inBoundVariables["fileContent"] as String?
            // Logic Here to decide if content is approved or not
            if (contentToProcess?.contains("activiti") == true) {
                integrationContext.addOutBoundVariable("approved",
                        true)
            } else {
                integrationContext.addOutBoundVariable("approved",
                        false)
            }
            integrationContext
        }
    }

    @Bean
    fun tagTextConnector(): Connector? {
        return Connector { integrationContext: IntegrationContext ->
            var contentToTag = integrationContext.inBoundVariables["fileContent"] as String?
            contentToTag += " :) "
            integrationContext.addOutBoundVariable("fileContent",
                    contentToTag)
            println("Final Content: $contentToTag")
            integrationContext
        }
    }

    @Bean
    fun discardTextConnector(): Connector? {
        return Connector { integrationContext: IntegrationContext ->
            var contentToDiscard = integrationContext.inBoundVariables["fileContent"] as String?
            contentToDiscard += " :( "
            integrationContext.addOutBoundVariable("fileContent",
                    contentToDiscard)
            println("Final Content: $contentToDiscard")
            integrationContext
        }
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            SpringApplication.run(ExampleApplication::class.java, *args)
        }
    }
}
package it.polito.wa2.server

import it.polito.wa2.server.products.ProductRepository
import it.polito.wa2.server.profiles.ProfileRepository
import it.polito.wa2.server.ticketing.employees.*
import it.polito.wa2.server.ticketing.employees.toDTO
import it.polito.wa2.server.ticketing.logs.LogRepository
import it.polito.wa2.server.ticketing.purchases.PurchaseRepository
import it.polito.wa2.server.ticketing.tickets.TicketRepository
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace=AutoConfigureTestDatabase.Replace.NONE)
class ExpertsTests {
    companion object {
        @Container
        val postgres = PostgreSQLContainer("postgres:latest")

        inline fun <reified T> typeReference() = object: ParameterizedTypeReference<T>() {}
        private const val baseUrl = "/API/experts"

        @JvmStatic
        @DynamicPropertySource
        fun properties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl)
            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)
            registry.add("spring.jpa.hibernate.ddl-auto") {"create-drop"}
        }

        private lateinit var expert1: Expert
        private lateinit var expert2: Expert
    }

    @Autowired
    lateinit var restTemplate: TestRestTemplate
    @Autowired
    lateinit var profileRepository: ProfileRepository
    @Autowired
    lateinit var productRepository: ProductRepository
    @Autowired
    lateinit var expertRepository: ExpertRepository
    @Autowired
    lateinit var expertSpecializationRepository: ExpertSpecializationRepository
    @Autowired
    lateinit var purchaseRepository: PurchaseRepository
    @Autowired
    lateinit var ticketRepository: TicketRepository
    @Autowired
    lateinit var logRepository: LogRepository

    @BeforeEach
    fun populateDb() {
        logRepository.deleteAll()
        ticketRepository.deleteAll()
        purchaseRepository.deleteAll()
        expertSpecializationRepository.deleteAll()
        expertRepository.deleteAll()
        profileRepository.deleteAll()
        productRepository.deleteAll()

        // recreate the objects before each test to reinitialize the ids
        expert1 = Expert("John", "Smith")
        expert2 = Expert("Jack", "Smith")
        expertRepository.save(expert1)
    }

    @Test
    fun getExpert() {
        val res = restTemplate.exchange("$baseUrl/${expert1.id}", HttpMethod.GET, null, typeReference<ExpertDTO>())

        Assertions.assertEquals(HttpStatus.OK, res.statusCode)
        Assertions.assertEquals(expert1.toDTO(), res.body)
    }

    @Test
    fun expertNotFound() {
        val res = restTemplate.exchange("$baseUrl/0", HttpMethod.GET, null, typeReference<Unit>())

        Assertions.assertEquals(HttpStatus.NOT_FOUND, res.statusCode)
    }

    @Test
    fun createExpert() {
        val requestEntity = HttpEntity(expert2.toNewDTO())
        val res = restTemplate.exchange(baseUrl, HttpMethod.POST, requestEntity, typeReference<ExpertDTO>())

        // expert2 with newly autogenerated id
        val createdExpert = expert2.apply { id = res.body!!.id }.toDTO()

        Assertions.assertEquals(HttpStatus.OK, res.statusCode)
        Assertions.assertEquals(createdExpert, res.body)

        val res2 = restTemplate.exchange("$baseUrl/${createdExpert.id}", HttpMethod.GET, null, typeReference<ExpertDTO>())
        Assertions.assertEquals(HttpStatus.OK, res2.statusCode)
        Assertions.assertEquals(createdExpert, res2.body)
    }

    @Test
    fun creatingAlreadyExistingExpertShouldNotFail() {
        val requestEntity = HttpEntity(expert1.toNewDTO())
        val res = restTemplate.exchange(baseUrl, HttpMethod.POST, requestEntity, typeReference<ExpertDTO>())

        // textExpert2 with newly autogenerated id
        val createdExpert = expert1.apply { id = res.body!!.id }.toDTO()

        Assertions.assertEquals(HttpStatus.OK, res.statusCode)
        Assertions.assertEquals(createdExpert, res.body)

        val res2 = restTemplate.exchange("$baseUrl/${createdExpert.id}", HttpMethod.GET, null, typeReference<ExpertDTO>())
        Assertions.assertEquals(HttpStatus.OK, res2.statusCode)
        Assertions.assertEquals(createdExpert, res2.body)
    }

    @Test
    fun editExpert() {
        // edit expert1 with expert2 fields
        val editedExpert = ExpertDTO(expert1.id, expert2.firstName, expert2.lastName, expert2.specializations.map { it.toDTO() }, expert2.tickets.map { it.id })
        val requestEntity = HttpEntity(editedExpert)
        val res = restTemplate.exchange(baseUrl, HttpMethod.PUT, requestEntity, typeReference<Unit>())

        Assertions.assertEquals(HttpStatus.OK, res.statusCode)

        val res2 = restTemplate.exchange("$baseUrl/${editedExpert.id}", HttpMethod.GET, null, typeReference<ExpertDTO>())
        Assertions.assertEquals(HttpStatus.OK, res2.statusCode)
        Assertions.assertEquals(editedExpert, res2.body)
    }

    @Test
    fun editExpertNotFound() {
        val editedExpert = ExpertDTO(0, expert2.firstName, expert2.lastName, expert2.specializations.map { it.toDTO() }, expert2.tickets.map { it.id })
        val requestEntity = HttpEntity(editedExpert)
        val res = restTemplate.exchange(baseUrl, HttpMethod.PUT, requestEntity, typeReference<Unit>())

        Assertions.assertEquals(HttpStatus.NOT_FOUND, res.statusCode)
    }

    @Test
    fun addExpertSpecialization() {
        val newSpecialization = "Computers"
        val requestEntity = HttpEntity(newSpecialization)
        val res = restTemplate.exchange("$baseUrl/${expert1.id}/specialization", HttpMethod.POST, requestEntity, typeReference<ExpertSpecializationDTO>())

        Assertions.assertEquals(HttpStatus.OK, res.statusCode)

        val expectedRes = ExpertDTO(
            expert1.id,
            expert1.firstName,
            expert1.lastName,
            listOf(ExpertSpecializationDTO(res.body!!.id, newSpecialization)),
            expert1.tickets.map { it.id }
        )

        val res2 = restTemplate.exchange("$baseUrl/${expert1.id}", HttpMethod.GET, null, typeReference<ExpertDTO>())

        Assertions.assertEquals(HttpStatus.OK, res2.statusCode)
        Assertions.assertEquals(expectedRes, res2.body)
    }

    @Test
    fun addExpertSpecializationExpertNotFound() {
        val newSpecialization = "Computers"
        val requestEntity = HttpEntity(newSpecialization)
        val res = restTemplate.exchange("$baseUrl/${expert2.id}/specialization", HttpMethod.POST, requestEntity, typeReference<Unit>())

        Assertions.assertEquals(HttpStatus.NOT_FOUND, res.statusCode)
    }

    @Test
    fun removeExpertSpecialization() {
        val expertSpecialization = ExpertSpecialization("Computers", expert1)
        expertSpecializationRepository.save(expertSpecialization)

        val requestEntity = HttpEntity(expertSpecialization.toDTO())

        val res = restTemplate.exchange("$baseUrl/specialization", HttpMethod.DELETE, requestEntity, typeReference<Unit>())
        Assertions.assertEquals(HttpStatus.OK, res.statusCode)

        val expectedRes = ExpertDTO(
            expert1.id,
            expert1.firstName,
            expert1.lastName,
            listOf(),
            expert1.tickets.map { it.id }
        )

        val res2 = restTemplate.exchange("$baseUrl/${expert1.id}", HttpMethod.GET, null, typeReference<ExpertDTO>())
        Assertions.assertEquals(HttpStatus.OK, res2.statusCode)
        Assertions.assertEquals(expectedRes, res2.body)
    }

    @Test
    fun removeExpertSpecializationNotFound() {
        val expertSpecialization = ExpertSpecialization("Computers", expert1)
        val requestEntity = HttpEntity(expertSpecialization.toDTO())

        val res = restTemplate.exchange("$baseUrl/specialization", HttpMethod.DELETE, requestEntity, typeReference<Unit>())
        Assertions.assertEquals(HttpStatus.NOT_FOUND, res.statusCode)
    }
}
package it.polito.wa2.server.employees

import it.polito.wa2.server.exceptions.ManagerNotFoundException
import it.polito.wa2.server.security.AuthenticationService
import jakarta.validation.constraints.Email
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class ManagerServiceImpl(
    private val managerRepository: ManagerRepository,
    private val authenticationService: AuthenticationService
): ManagerService {

    override fun getManager(id: String): ManagerDTO {
        return managerRepository.findByIdOrNull(id)?.toManagerDTO() ?: throw ManagerNotFoundException()
    }

    override fun getManagerByEmail(@Email email: String): ManagerDTO {
        return managerRepository.findByEmail(email)?.toManagerDTO() ?: throw ManagerNotFoundException()
    }

    override fun editManager(managerDTO: ManagerDTO) {
        val manager = managerRepository.findByIdOrNull(managerDTO.id) ?: throw ManagerNotFoundException()

        // modify all fields except id
        manager.firstName = managerDTO.firstName
        manager.lastName = managerDTO.lastName

        // modify name in Keycloak
        authenticationService.editName(managerDTO.id, managerDTO.firstName, managerDTO.lastName)

        managerRepository.save(manager)
    }
}
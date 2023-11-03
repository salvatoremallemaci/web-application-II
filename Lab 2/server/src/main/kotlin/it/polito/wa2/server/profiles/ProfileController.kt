package it.polito.wa2.server.profiles

import it.polito.wa2.server.exceptions.IncoherentParametersException
import it.polito.wa2.server.exceptions.ProfileNotFoundException
import jakarta.validation.Valid
import jakarta.validation.constraints.Email
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@RestController
@CrossOrigin("http://localhost:3000")
@Validated
class ProfileController(
    private val profileService: ProfileService
) {
    @GetMapping("/API/profiles/{email}")
    fun getProfileById(@PathVariable @Email email: String): ProfileDTO {
        return profileService.getProfile(email)
    }

    @PostMapping("/API/profiles")
    fun createProfile(@RequestBody @Valid profileDTO: ProfileDTO) {
        profileService.createProfile(profileDTO)
    }

    @PutMapping("/API/profiles/{email}")
    fun editProfile(@PathVariable @Email email: String, @RequestBody @Valid profileDTO: ProfileDTO) {
        if (email != profileDTO.email)
            throw IncoherentParametersException()

        profileService.editProfile(profileDTO)
    }
}
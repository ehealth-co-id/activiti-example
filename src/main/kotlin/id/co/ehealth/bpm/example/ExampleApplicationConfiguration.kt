package id.co.ehealth.bpm.example

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.provisioning.InMemoryUserDetailsManager
import java.util.*
import java.util.Arrays.asList
import java.util.stream.Collectors


@Configuration
@EnableWebSecurity
class ExampleApplicationConfiguration : WebSecurityConfigurerAdapter() {
    private val logger = LoggerFactory.getLogger(ExampleApplicationConfiguration::class.java)

    @Autowired
    @Throws(Exception::class)
    override fun configure(auth: AuthenticationManagerBuilder) {
        auth.userDetailsService(myUserDetailsService())
    }

    @Bean
    fun myUserDetailsService(): UserDetailsService {
        val inMemoryUserDetailsManager = InMemoryUserDetailsManager()
        val usersGroupsAndRoles = arrayOf(arrayOf("bob", "password", "ROLE_ACTIVITI_USER", "GROUP_activitiTeam"), arrayOf("john", "password", "ROLE_ACTIVITI_USER", "GROUP_activitiTeam"), arrayOf("hannah", "password", "ROLE_ACTIVITI_USER", "GROUP_activitiTeam"), arrayOf("other", "password", "ROLE_ACTIVITI_USER", "GROUP_otherTeam"), arrayOf("admin", "password", "ROLE_ACTIVITI_ADMIN"))
        for (user in usersGroupsAndRoles) {
            val authoritiesStrings: List<String> = user.copyOfRange(2, user.size).toList()
            logger.info("> Registering new user: " + user[0] + " with the following Authorities[" + authoritiesStrings + "]")
            inMemoryUserDetailsManager.createUser(User(user[0], passwordEncoder().encode(user[1]),
                    authoritiesStrings.stream().map { s: String? -> SimpleGrantedAuthority(s) }.collect(Collectors.toList())))
        }
        return inMemoryUserDetailsManager
    }

    @Throws(Exception::class)
    override fun configure(http: HttpSecurity) {
        http
                .csrf().disable()
                .authorizeRequests()
                .anyRequest()
                .authenticated()
                .and()
                .httpBasic()
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }
}
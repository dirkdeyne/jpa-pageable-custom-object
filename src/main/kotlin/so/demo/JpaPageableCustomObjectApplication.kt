package so.demo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.boot.runApplication
import org.springframework.context.event.EventListener
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import javax.persistence.*


@SpringBootApplication
class JpaPageableCustomObjectApplication

fun main(args: Array<String>) {
    runApplication<JpaPageableCustomObjectApplication>(*args)
}

@Entity data class User(@Id val account : String, val name : String, val active : Boolean)
@Entity data class Email(@Id val email : String, @ManyToOne val user : User)

data class UserEmail(val name:String, val email:String)

interface UserRepo : PagingAndSortingRepository<User, String>

interface EmailRepo : PagingAndSortingRepository<Email, String> {

    @Query("select new so.demo.UserEmail( e.user.name, e.email) from Email e where e.user.active = :active" )
    fun findAllActiveUserName(@Param("active") active : Boolean, pageable : Pageable) : Page<UserEmail>

    @Query("select new so.demo.UserEmail( e.user.name as name, e.email) from Email e where e.user.active = :active" )
    fun findAllActiveUserEmailWithNameAlias(@Param("active") active : Boolean, pageable : Pageable) : Page<UserEmail>
}

@Component
class InitializeData (private val userRepo : UserRepo, private val emailRepo: EmailRepo){

    @EventListener(ApplicationReadyEvent::class)
    fun init(){

        val foo = userRepo.save(User("foo.b", "Foo Bar", true))
        emailRepo.save(Email("foo.b@so.com" , foo))

        val bar = userRepo.save(User("bar.f", "Bar Foo", false))
        emailRepo.save(Email("bar.f@so.com" , bar))

        val dirk = userRepo.save(User("deyne.d", "Deyne Dirk", true))
        emailRepo.save(Email("deyne.d@so.com" , dirk))

        val pageable =  PageRequest.of(0, 2, Sort.by("email"));

        emailRepo.findAllActiveUserEmailWithNameAlias(true, pageable).forEach { println(it) }
        emailRepo.findAllActiveUserEmailWithNameAlias(false, pageable).forEach { println(it) }
    }
}

@RestController
class ApiController (private val userRepo : UserRepo, private val emailRepo: EmailRepo){
    @GetMapping("/alias/active")
    fun allActiveUserEmailWithNameAlias(pageable: Pageable) = emailRepo.findAllActiveUserEmailWithNameAlias(true, pageable)

    @GetMapping("/alias/inactive")
    fun allInActiveUserEmailWithNameAlias(pageable: Pageable) = emailRepo.findAllActiveUserEmailWithNameAlias(false, pageable)

    @GetMapping("/normal/active")
    fun allActiveUserName(pageable: Pageable) = emailRepo.findAllActiveUserName(true, pageable)

    @GetMapping("/normal/inactive")
    fun allInActiveUserName(pageable: Pageable) = emailRepo.findAllActiveUserName(false, pageable)
}

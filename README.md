Stackoverflow question: [jpql-query-on-sorting-not-taking-column-name-directly-on-pageable-sort-value](https://stackoverflow.com/questions/59015501/jpql-query-on-sorting-not-taking-column-name-directly-on-pageable-sort-value/59016963?noredirect=1#comment104308875_59016963)

Use alias in your query to allow requests like [http://localhost:8080/alias/active?sort=name,asc](http://localhost:8080/alias/active?sort=name,asc) instead of [http://localhost:8080/normal/active?sort=user.name,asc](http://localhost:8080/normal/active?sort=user.name,asc)

Using a query without the use of an alias like
```kotlin
@Query("select new so.demo.UserEmail( e.user.name, e.email) from Email e where e.user.active = :active" )
fun findAllActiveUserName(@Param("active") active : Boolean, pageable : Pageable) : Page<UserEmail>
```
When want a sorted pageable, you are forced to provide sort like _../active?sort=**user.name**,asc_


What we really want is just use _../active?sort=**sort=name**,asc_
That is possible by using an alias in our query

```kotlin    
@Query("select new so.demo.UserEmail( e.user.name as name, e.email) from Email e where e.user.active = :active" )
fun findAllActiveUserEmailWithNameAlias(@Param("active") active : Boolean, pageable : Pageable) : Page<UserEmail>
```

possible requests:
use normal query
[http://localhost:8080/normal/active?sort=user.name,asc](http://localhost:8080/normal/active?sort=user.name,asc)
[http://localhost:8080/normal/inactive?sort=user.name,asc](http://localhost:8080/normal/inactive?sort=user.name,asc)
use alias in query
[http://localhost:8080/alias/active?sort=name,asc](http://localhost:8080/alias/active?sort=name,asc)
[http://localhost:8080/alias/inactive?sort=name,asc](http://localhost:8080/alias/inactive?sort=name,asc)

Note: when using alias using 'user.name' is also valid [http://localhost:8080/alias/active?sort=user.name,asc](http://localhost:8080/alias/active?sort=user.name,asc)
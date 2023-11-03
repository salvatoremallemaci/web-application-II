package it.polito.wa2.server.chat

import it.polito.wa2.server.customers.Person
import jakarta.persistence.*
import java.time.ZonedDateTime

@Entity
@Table(name = "messages")
class Message(
    @Column(length = 65536)
    val text: String,
    val time: ZonedDateTime,
    @OneToOne
    val from: Person,
    @ManyToOne
    val chat: Chat
) {
    @Id
    @GeneratedValue
    @Column(updatable = false, nullable = false)
    var id: Int = 0
}
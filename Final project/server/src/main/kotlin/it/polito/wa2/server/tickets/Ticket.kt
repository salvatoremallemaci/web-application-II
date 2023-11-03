package it.polito.wa2.server.tickets

import it.polito.wa2.server.chat.Chat
import it.polito.wa2.server.employees.Expert
import it.polito.wa2.server.purchases.Purchase
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToOne
import jakarta.persistence.Table

enum class TicketStatus {
    OPEN, IN_PROGRESS, CLOSED, REOPENED, RESOLVED
}

enum class PriorityLevel {
    LOW, NORMAL, HIGH, CRITICAL
}

@Entity
@Table(name = "tickets")
class Ticket (
    var title: String,
    var description: String,
    @ManyToOne
    val purchase: Purchase
) {
    @Id
    @GeneratedValue
    @Column(updatable = false, nullable = false)
    var id: Int = 0
    @ManyToOne
    var expert: Expert? = null
    var ticketStatus = TicketStatus.OPEN
    var priorityLevel = PriorityLevel.NORMAL
    @OneToOne
    var chat: Chat? = null
}
package it.polito.wa2.server.products

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import jakarta.persistence.Id

@Entity
@Table(name = "products")
class Product (
    @Id
    @Column(updatable = false, nullable = false)
    val ean: String,
    var name: String,
    var brand: String,
)
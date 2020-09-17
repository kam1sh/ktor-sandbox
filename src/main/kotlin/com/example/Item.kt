package com.example

import javax.persistence.*

@Entity
@Table(name = "items")
data class Item(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column
    val data: String
)
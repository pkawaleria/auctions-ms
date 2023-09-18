package pl.kawaleria.auctsys

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class AuctsysApplication

fun main(args: Array<String>) {
	runApplication<AuctsysApplication>(*args)
}

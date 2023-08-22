package pl.kawaleria.auctsys

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableAsync

@SpringBootApplication
@EnableAsync
class AuctsysApplication

fun main(args: Array<String>) {
	runApplication<AuctsysApplication>(*args)
}

package pl.kawaleria.auctsys

import com.github.cloudyrock.spring.v5.EnableMongock
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableMongock
class AuctsysApplication

fun main(args: Array<String>) {
	runApplication<AuctsysApplication>(*args)
}

package pl.kawaleria.auctsys.views.domain

import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneId

class MutableClock(initialInstant: Instant, private val zone: ZoneId) : Clock() {
    private var currentInstant = initialInstant

    fun advanceBy(duration: Duration) {
        currentInstant = currentInstant.plus(duration)
    }

    override fun instant(): Instant = currentInstant

    override fun getZone(): ZoneId = zone

    override fun withZone(zone: ZoneId): Clock {
        return MutableClock(currentInstant, zone)
    }
}

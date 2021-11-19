package no.nav.dagpenger.vedtak.modell.tid.quantity

// Understands a specific metric
class Unitt {
    companion object {
        internal val teaspoon = Unitt()
        internal val tablespoon = Unitt(3, teaspoon)
        internal val ounce = Unitt(2, tablespoon)
        internal val cup = Unitt(8, ounce)
        internal val pint = Unitt(2, cup)
        internal val quart = Unitt(2, pint)
        internal val gallon = Unitt(4, quart)

        internal val inch = Unitt()
        internal val foot = Unitt(12, inch)
        internal val yard = Unitt(3, foot)
        internal val chain = Unitt(22, yard)
        internal val link = Unitt(0.01, chain)
        internal val furlong = Unitt(10, chain)
        internal val mile = Unitt(8, furlong)

        internal val celsius = Unitt()
        internal val fahrenheit = Unitt(5/9.0, 32, celsius)
        internal val gasMark = Unitt(125/9.0, -218.0/25, celsius)
        internal val kelvin = Unitt(1, 273.15, celsius)
        internal val rankine = Unitt(5/9.0, 491.67, celsius)

        internal val unit = Unitt()
        internal val times = Unitt(1, unit)
        internal val percentage = Unitt(0.01, unit)
        
        internal val second = Unitt()
        internal val minute = Unitt(60, second)
        internal val hour = Unitt(60, minute)

        internal val day = Unitt(24, hour)
        internal val week = Unitt(5, day)
    }
    private val baseUnit: Unitt
    private val baseUnitRatio: Double
    private val offset: Double

    private constructor() {
        baseUnit = this
        baseUnitRatio = 1.0
        offset = 0.0
    }

    private constructor(relativeRatio: Number, relativeUnit: Unitt) :
                this(relativeRatio, 0.0, relativeUnit)

    private constructor(relativeRatio: Number, offset: Number, relativeUnit: Unitt) {
        baseUnit = relativeUnit.baseUnit
        baseUnitRatio = relativeRatio.toDouble() * relativeUnit.baseUnitRatio
        this.offset = offset.toDouble()
    }

    internal fun convertedAmount(otherAmount: Double, other: Unitt): Double {
        require(this.isCompatible(other)) { "Incompatible Unit types" }
        return (otherAmount - other.offset) * other.baseUnitRatio / this.baseUnitRatio + this.offset
    }

    internal fun hashCode(amount: Double) = ((amount - offset) * baseUnitRatio).hashCode()

    internal fun isCompatible(other: Unitt) = this.baseUnit == other.baseUnit

}

val Number.teaspoons get(): RatioQuantity = SpecificQuantity(this, Unitt.teaspoon)
val Number.tablespoons get(): RatioQuantity = SpecificQuantity(this, Unitt.tablespoon)
val Number.ounces get(): RatioQuantity = SpecificQuantity(this, Unitt.ounce)
val Number.cups get(): RatioQuantity = SpecificQuantity(this, Unitt.cup)
val Number.pints get(): RatioQuantity = SpecificQuantity(this, Unitt.pint)
val Number.quarts get(): RatioQuantity = SpecificQuantity(this, Unitt.quart)
val Number.gallons get(): RatioQuantity = SpecificQuantity(this, Unitt.gallon)

val Number.inches get(): RatioQuantity = SpecificQuantity(this, Unitt.inch)
val Number.feet get(): RatioQuantity = SpecificQuantity(this, Unitt.foot)
val Number.yards get(): RatioQuantity = SpecificQuantity(this, Unitt.yard)
val Number.chains get(): RatioQuantity = SpecificQuantity(this, Unitt.chain)
val Number.links get(): RatioQuantity = SpecificQuantity(this, Unitt.link)
val Number.furlongs get(): RatioQuantity = SpecificQuantity(this, Unitt.furlong)
val Number.miles get(): RatioQuantity = SpecificQuantity(this, Unitt.mile)

val Number.celsius get(): IntervalQuantity = IntervalQuantity(this, Unitt.celsius)
val Number.fahrenheit get(): IntervalQuantity = IntervalQuantity(this, Unitt.fahrenheit)
val Number.gasMark get(): IntervalQuantity = IntervalQuantity(this, Unitt.gasMark)
val Number.kelvin get(): IntervalQuantity = IntervalQuantity(this, Unitt.kelvin)
val Number.rankine get(): IntervalQuantity = IntervalQuantity(this, Unitt.rankine)

val Number.units get(): RatioQuantity = SpecificQuantity(this, Unitt.unit)
val Number.times get(): RatioQuantity = SpecificQuantity(this, Unitt.times)
val Number.percent get(): RatioQuantity = SpecificQuantity(this, Unitt.percentage)

val Number.seconds get(): RatioQuantity = SpecificQuantity(this, Unitt.second)
val Number.minutes get(): RatioQuantity = SpecificQuantity(this, Unitt.minute)
val Number.hours get(): RatioQuantity = SpecificQuantity(this, Unitt.hour)
val Number.days get(): RatioQuantity = SpecificQuantity(this, Unitt.day)
val Number.weeks get(): RatioQuantity = SpecificQuantity(this, Unitt.week)
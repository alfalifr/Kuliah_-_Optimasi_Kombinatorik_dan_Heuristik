import org.junit.Test
import sidev.lib.console.prin
import kotlin.math.exp
import kotlin.random.Random
import sidev.lib.progression.progressTo
import java.lang.Math.pow
import kotlin.math.log10
import kotlin.math.pow

class UnitTest {
    @Test
    fun expTest(){
        prin(exp(1.0))
        prin(exp(2.0))
        val delta= 3.0
        val t= 10.0
        val acc = 1 / (1 + exp(delta/t))
        prin(acc)
        listOf(1,3,1).random()
        //(0..1).random(Random.Default.nex)
    }

    @Test
    fun mapRemoveTest(){
        val map= hashMapOf<String, Int>()
        map["a"]= 1
        map["b"]= 2
        map["c"]= 3
        prin("map= $map")
        map.remove("b")
        prin("map= $map")
        map.forEach { t, u -> prin("t= $t u= $u") }
    }

    @Test
    fun randomTempTest(){
        prin("exp(1.0)= ${exp(1.0)}")
        prin("exp(1.0)= ${log10(1.0)}")
        val f1= { it: Double -> 1 / (1 + exp(1.0 / it)) }
        val f2= { it: Double -> 1 / exp(1.0 / it) }
        val f3= { it: Double -> 1 / (1-exp(1.0 / it)) }
        val f4= { it: Double -> 1 / 234.567.pow(1/it) }
        val f5= { it: Double -> 1 / (1+23.518 * (1/it)) }
        val f6= { it: Double -> exp(-1.0 / it) }
        val f7= { it: Double -> log10(it) }
        val f8= { it: Double -> 1 / -log10(it) }


        val limit= 100
        val coef= limit * 15 / 100.0  // persen ke berapa probabilitas mencapai 50%, dalam kasus di samping, yaitu sebesar 15% dari panjang sudah mencapai 50% probabilitasnya.
        val f5_= { it: Double -> 1 / (2 + coef * (1/it)) }
        for(i in 1.progressTo(limit, 1)){
            val d= i.toDouble()
            prin("i= $i f1= ${f1(d)}")
            prin("i= $i f2= ${f2(d)}")
            prin("i= $i f3= ${f3(d)}")
            prin("i= $i f4= ${f4(d)}")
            prin("i= $i f5= ${f5(d)}")
            prin("i= $i f5_= ${f5_(d)}")
            prin("====================")
        }
    }
}
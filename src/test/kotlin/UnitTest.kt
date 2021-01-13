import org.junit.Test
import sidev.lib.console.prin
import kotlin.math.exp
import kotlin.random.Random

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
}
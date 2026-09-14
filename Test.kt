import jcifs.context.SingletonContext
import jcifs.netbios.NbtAddress

fun main() {
    val context = SingletonContext.getInstance()
    val addr = context.nameServiceClient.getByName("192.168.1.1")
    println(addr.javaClass.name)
}

package DingDingExercise
/**
        100 ~ 999 所有水仙花数
        各位数字的立方和刚好等于该数本身
*/

object exercise1 {
    def main(args: Array[String]): Unit = {
        for (i <- 100 until 1000) {
            val a: Int = i / 100 % 10
            val b: Int = i / 10 % 10
            val c: Int = i % 10
            if (a * a * a + b * b * b + c * c * c == i) {
                println(i);
            }
        }
    }
}

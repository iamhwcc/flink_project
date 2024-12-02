package DataStream_API.Transformation

import org.apache.flink.api.java.functions.KeySelector
import org.apache.flink.streaming.api.scala._
import pojo.Event


object keyBy_sum {
    def main(args: Array[String]): Unit = {
        val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
        val stream: DataStream[Event] = env.fromElements(Event("Mary", "./home", 1000L),
            Event("Bob", "./about", 1020L), Event("Jack", "./test", 1200L),
            Event("Mary", "./prod?id=1", 3000L),
            Event("Mary", "./adds?password=mary01234", 1256L)
        )
        //1. 直接写一个匿名函数
        //        val group: KeyedStream[Event, String] = stream.keyBy(_.name)

        //2. 自定义KeySelector
        //KeyedStream : 按key分组的流
        val group: KeyedStream[Event, String] = stream.keyBy(new myKeySelector())
        group.print()
        env.execute()
    }

    //传入泛型 KeySelector<IN, KEY> => <进入的数据类型 要按哪个key分组>
    class myKeySelector extends KeySelector[Event, String] {
        //进入的是Event类的数据类型，根据Event类中name来作为key分组
        override def getKey(in: Event): String = in.name
    }

}

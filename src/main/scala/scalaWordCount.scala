import java.io.File
import scala.collection.mutable
import scala.io.{BufferedSource, Source}

object scalaWordCount {
    def main(args: Array[String]): Unit = {
        val file = new File("datas/word.txt")
        val data: BufferedSource = Source.fromFile(file)
        val myMap: mutable.Map[String, Int] = mutable.Map[String, Int]()
        val words: Iterator[String] = data.getLines().flatMap(_.split(" "))
        words.foreach(word=>{
            if(myMap.contains(word)){
                myMap(word)+=1
            }else{
                myMap(word)=1
            }
        })
        myMap.foreach(println)
    }
}

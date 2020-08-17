import akka.actor.Actor
import akka.actor.ActorSystem
import akka.actor.Props
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import akka.actor.SuppressedDeadLetter


object Main2 extends App{


    val system = ActorSystem.apply("supervisonSystem")

    val config = ConfigFactory.load()


    val supervisorRef = system.actorOf(Props[SupervisorActor].withMailbox("my-mailbox"))

    system.eventStream.subscribe(supervisorRef, classOf[SuppressedDeadLetter])

    import scala.concurrent.duration._
    import scala.concurrent.ExecutionContext.Implicits.global

    implicit val timeout : Timeout = 10.seconds

    1 to 100 foreach{
        e =>
            val res =(supervisorRef ? e).mapTo[String]
            res.onComplete{
                tr =>
                    println(tr.get)
            }
            if(e==100){
                println("All messages sent to Supervisor")
            }
    }


}

case class Message(sender:ActorRef,msg:Int)

class SupervisorActor extends Actor{

  override def receive: Actor.Receive = {
      case msg:Int => 
        context.actorOf(Props[WorkerActor]) ! Message(sender(),msg)
  }


}


class WorkerActor extends Actor{
    override def receive: Actor.Receive = {
      case Message(sender:ActorRef,msg:Int) => 
        Thread.sleep(5000)
        sender ! "Done :::"+msg
        

  }
}


##### rsources/application.conf
my-mailbox{
  mailbox-type = "akka.dispatch.NonBlockingBoundedMailbox"
  mailbox-capacity = 10
}



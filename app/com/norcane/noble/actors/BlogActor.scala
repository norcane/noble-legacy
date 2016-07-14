package com.norcane.noble.actors

import akka.actor.{Actor, Props}
import akka.actor.Actor.Receive

class BlogActor extends Actor {

  override def receive: Receive = PartialFunction.empty
}

object BlogActor {
  def props: Props = Props(new BlogActor)
}
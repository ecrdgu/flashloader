package com.dongguk.ecr.common.event;

/**
 * Event: A base class which contains an event object.
 * @author jhun.ahn
 *
 */
public class Event {

  protected Object source;

  public Event(Object source) {
    this.source = source;
  }

  public Object source() {
    return this.source;
  }
}

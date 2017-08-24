package com.dongguk.ecr.framework.common.event;

import com.dongguk.ecr.common.event.Event;

/**
 * A EventBus is to provide publish/subscribe style communication between
 * components.
 * @author jhun.ahn
 *
 */
public interface IEventBus {

  /**
   * Starts the event bus.
   */
  void start();

  /**
   * Returns whether or not this EventBus is started
   *
   * @return
   */
  boolean isStarted();

  /**
   * Terminates event bus.
   */
  void terminate();

  /**
   * Adds a listener to receive 'id' events.
   *
   * @param id
   * @param listener
   * @return
   */
  boolean addListener(int id, IEventBusListener listener);

  /**
   * Adds a listener which receives all events from the bus.
   *
   * @param listener
   * @return
   */
  boolean addListener(IEventBusListener listener);

  /**
   * Removes a listener to stop receiving 'id' event.
   *
   * @param id
   * @param listener
   * @return
   */
  boolean removeListener(int id, IEventBusListener listener);

  /**
   * Removes a listener.
   *
   * @param listener
   * @return
   */
  boolean removeListener(IEventBusListener listener);

  /**
   * Returns a name of bus.
   *
   * @return
   */
  String name();

  /**
   * Sends event to this manager's group
   *
   * @param event
   */
  void send(int id, Event event);

  /**
   * Sends an event (no event object) to this manager's group
   *
   * @param event
   */
  void send(int id);

  /**
   * Sends event to this manager's group asynchronously
   *
   * @param id
   * @param event
   */
  void post(int id, Event event);
}

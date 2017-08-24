package com.dongguk.ecr.framework.common.event;

import com.dongguk.ecr.common.event.Event;

/**
 * The listener interface for receiving events from {@link IEventBus}
 *
 * @author jhun.ahn
 *
 */
public interface IEventBusListener {

  /**
   * Invoked when an event is delivered to EventBus with an actual event object.
   *
   * @param id
   * @param t
   */
  void handleEvent(int id, Event t);
}

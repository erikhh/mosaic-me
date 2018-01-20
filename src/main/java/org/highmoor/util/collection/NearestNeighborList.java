package org.highmoor.util.collection;

// Bjoern Heckel's solution to the KD-Tree n-nearest-neighbor problem

class NearestNeighborList {

  public static int REMOVE_HIGHEST = 1;
  public static int REMOVE_LOWEST = 2;

  PriorityQueue queue = null;
  int capacity = 0;

  // constructor
  public NearestNeighborList(int capacity) {
    this.capacity = capacity;
    this.queue = new PriorityQueue(capacity, Double.POSITIVE_INFINITY);
  }

  public double getMaxPriority() {
    if (queue.length() == 0) {
      return Double.POSITIVE_INFINITY;
    }
    return queue.getMaxPriority();
  }

  public boolean insert(Object object, double priority) {
    if (queue.length() < capacity) {
      // capacity not reached
      queue.add(object, priority);
      return true;
    }
    if (priority > queue.getMaxPriority()) {
      // do not insert - all elements in queue have lower priority
      return false;
    }
    // remove object with highest priority
    queue.remove();
    // add new object
    queue.add(object, priority);
    return true;
  }

  public boolean isCapacityReached() {
    return queue.length() >= capacity;
  }

  public Object getHighest() {
    return queue.front();
  }

  public boolean isEmpty() {
    return queue.length() == 0;
  }

  public int getSize() {
    return queue.length();
  }

  public Object removeHighest() {
    // remove object with highest priority
    return queue.remove();
  }
}

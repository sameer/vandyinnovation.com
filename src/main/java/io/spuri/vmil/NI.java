package io.spuri.vmil;

import java.util.PriorityQueue;
import java.util.function.Function;

public class NI implements Comparable<NI> {
  public static NIModifier identity() {
    return (NI ni) -> ni;
  }

  public String title, link;
  public int priority;
  public NI parent = null;
  public PriorityQueue<NI> children = new PriorityQueue<>();

  public NI title(String title) {
    this.title = title;
    return this;
  }

  public NI link(String link) {
    this.link = link;
    return this;
  }

  public NI priority(int priority) {
    this.priority = priority;
    return this;
  }

  public NI parent(NI parent) {
    this.parent = parent;
    return this;
  }
  public NI child(NI child) {
    children.add(child);
    return this;
  }

  public static NI navItem() {
    return new NI();
  }

  @Override
  public int compareTo(NI navItem) {
    return this.priority - navItem.priority;
  }
}
@FunctionalInterface
interface NIModifier {
  NI modify(NI ni);
}


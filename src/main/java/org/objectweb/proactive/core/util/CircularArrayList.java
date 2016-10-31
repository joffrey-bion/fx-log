/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.core.util;

import java.util.AbstractList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.jetbrains.annotations.NotNull;

/**
 * <p>
 * Originally written by Dr. Heinz Kabutz in the very excellent
 * <a href="http://www.smotricz.com/kabutz/">The Java Specialists Newsletter</a>
 * </p><p>
 * Cleaned from many infamous bugs and completed.
 * </p>
 *
 * @author The ProActive Team
 * @version 1.0,  2001/10/23
 * @since ProActive 0.9
 */

public class CircularArrayList<E> extends AbstractList<E> implements List<E>, java.io.Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 51L;

    private static final int DEFAULT_SIZE = 5;

    protected E[] array;

    // head points to the first logical element in the array, and
    // tail points to the element following the last.  This means
    // that the list is empty when head == tail.  It also means
    // that the array array has to have an extra space in it.
    protected int head = 0;

    // head points to the first logical element in the array, and
    // tail points to the element following the last.  This means
    // that the list is empty when head == tail.  It also means
    // that the array array has to have an extra space in it.
    protected int tail = 0;

    // Strictly speaking, we don't need to keep a handle to size,
    // as it can be calculated programmatically, but keeping it
    // makes the algorithms faster.
    protected int size = 0;

    public CircularArrayList() {
        this(DEFAULT_SIZE);
    }

    @SuppressWarnings("unchecked")
    public CircularArrayList(int size) {
        array = (E[]) new Object[size];
    }

    @SuppressWarnings("unchecked")
    public CircularArrayList(Collection<E> c) {
        size = c.size();
        tail = c.size();
        array = (E[]) new Object[c.size()];
        c.toArray(array);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("CircularArray size=");
        sb.append(size);
        sb.append("\n");
        for (int i = 0; i < size; i++) {
            sb.append("[");
            sb.append(convert(i));
            sb.append("]=>");
            sb.append(array[convert(i)]);
            sb.append(", ");
        }
        sb.append("\n");
        return sb.toString();
    }

    @Override
    public boolean isEmpty() {
        return head == tail; // or size == 0
    }

    // We use this method to ensure that the capacity of the
    // list will suffice for the number of elements we want to
    // insert.  If it is too small, we make a new, bigger array
    // and copy the old elements in.
    @SuppressWarnings("unchecked")
    public void ensureCapacity(int minCapacity) {
        int oldCapacity = array.length;
        if (minCapacity > oldCapacity) {
            int newCapacity = ((oldCapacity * 3) / 2) + 1;
            if (newCapacity < minCapacity) {
                newCapacity = minCapacity;
            }
            E[] newData = (E[]) new Object[newCapacity];
            toArray(newData);
            tail = size;
            head = 0;
            array = newData;
        }
    }

    @Override
    public int size() {
        // the size can also be worked out each time as:
        // (tail + array.length - head) % array.length
        return size;
    }

    @Override
    public boolean contains(Object elem) {
        return indexOf(elem) >= 0;
    }

    @Override
    public int indexOf(Object elem) {
        if (elem == null) {
            for (int i = 0; i < size; i++) {
                if (array[convert(i)] == null) {
                    return i;
                }
            }
        } else {
            for (int i = 0; i < size; i++) {
                if (elem.equals(array[convert(i)])) {
                    return i;
                }
            }
        }
        return -1;
    }

    @Override
    public int lastIndexOf(Object elem) {
        if (elem == null) {
            for (int i = size - 1; i >= 0; i--) {
                if (array[convert(i)] == null) {
                    return i;
                }
            }
        } else {
            for (int i = size - 1; i >= 0; i--) {
                if (elem.equals(array[convert(i)])) {
                    return i;
                }
            }
        }
        return -1;
    }

    @NotNull
    @Override
    public Object[] toArray() {
        return toArray(new Object[size]);
    }

    @NotNull
    @Override
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(@NotNull T[] result) {
        if (size == 0) {
            return result;
        }
        if (result.length < size) {
            result = (T[]) java.lang.reflect.Array.newInstance(result.getClass().getComponentType(), size);
        }
        if (head < tail) {
            System.arraycopy(array, head, result, 0, tail - head);
        } else {
            System.arraycopy(array, head, result, 0, array.length - head);
            System.arraycopy(array, 0, result, array.length - head, tail);
        }
        if (result.length > size) {
            result[size] = null;
        }
        return result;
    }

    @Override
    public E get(int index) {
        rangeCheck(index);
        return array[convert(index)];
    }

    @Override
    public E set(int index, E element) {
        modCount++;
        rangeCheck(index);
        int convertedIndex = convert(index);
        E oldValue = array[convertedIndex];
        array[convertedIndex] = element;
        return oldValue;
    }

    @Override
    public boolean add(E o) {
        modCount++;
        // We have to have at least one empty space
        ensureCapacity(size + 1 + 1);
        array[tail] = o;
        tail = (tail + 1) % array.length;
        size++;
        return true;
    }

    // This method is the main reason we re-wrote the class.
    // It is optimized for removing first and last elements
    // but also allows you to remove in the middle of the list.
    @Override
    public E remove(int index) {
        modCount++;
        rangeCheck(index);
        int pos = convert(index);

        // an interesting application of try/finally is to avoid
        // having to use local variables
        try {
            return array[pos];
        } finally {
            array[pos] = null; // Let gc do its work
            // optimized for FIFO access, i.e. adding to back and
            // removing from front

            if (pos == head) {
                head = (head + 1) % array.length;
            } else if (pos == tail) {
                tail = (tail - 1 + array.length) % array.length;
            } else {
                if ((pos > head) && (pos > tail)) { // tail/head/pos
                    System.arraycopy(array, head, array, head + 1, pos - head);
                    head = (head + 1) % array.length;
                } else {
                    System.arraycopy(array, pos + 1, array, pos, tail - pos - 1);
                    tail = (tail - 1 + array.length) % array.length;
                }
            }
            size--;
        }
    }

    @Override
    public void clear() {
        modCount++;
        // Let gc do its work
        for (int i = 0; i != size; i++) {
            array[convert(i)] = null;
        }
        head = tail = size = 0;
    }

    @Override
    public void add(int index, E element) {
        if (index == size) {
            add(element);
            return;
        }
        modCount++;
        rangeCheck(index);
        // We have to have at least one empty space
        ensureCapacity(size + 1 + 1);
        int pos = convert(index);
        if (pos == head) {
            head = (head - 1 + array.length) % array.length;
            array[head] = element;
        } else if (pos == tail) {
            array[tail] = element;
            tail = (tail + 1) % array.length;
        } else {
            if ((pos > head) && (pos > tail)) { // tail/head/pos
                System.arraycopy(array, head, array, head - 1, pos - head);
                array[pos - 1] = element;
                head--;
            } else { // head/pos/tail
                System.arraycopy(array, pos, array, pos + 1, tail - pos);
                tail = (tail + 1) % array.length;
                array[pos] = element;
            }
        }

        size++;
    }

    @Override
    public boolean addAll(int index, @NotNull Collection<? extends E> collection) {
        modCount++;
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }

        int numNew = collection.size();
        ensureCapacity(size + numNew + 1);

        // shift old data
        for (int j = size; j >= index; j--) {
            int src = this.convert(j);
            int dst = this.convert(j + numNew);
            array[dst] = array[src];
        }

        // insert new data
        Iterator<? extends E> it = collection.iterator();
        for (int i = 0; i < numNew; i++) {
            array[this.convert(index + i)] = it.next();
        }

        // Fix tail & size
        size += numNew;
        tail = (head + size) % array.length;
        return true;
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends E> collection) {
        return this.addAll(this.size, collection);
    }

    // The convert() method takes a logical index (as if head was
    // always 0) and calculates the index within array
    private int convert(int index) {
        return (index + head) % array.length;
    }

    private void rangeCheck(int index) {
        if ((index >= size) || (index < 0)) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
    }
}

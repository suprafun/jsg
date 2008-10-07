/*
 * Copyright (c) 2008 Java Scene Graph
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'jMonkeyEngine' nor the names of its contributors 
 *   may be used to endorse or promote products derived from this software 
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package trb.jsg.util;

import java.io.Serializable;


/**
 * An auto resizeing array.
 */
public class ObjectArray<E> implements Serializable {

	private static final long serialVersionUID = 0L;
	
	// the maximum size of the array.
	private static final int MAX_SIZE = 1024*4;
	
	// the wrapped array
	private Object array[] = new Object[0];
	
	// the length of the array counting to the last non null element
	private int length = 0;
	
	
	/**
	 * Sets the object at the specified index.
	 * @param o the object to set
	 * @param index the index into the array
	 * @return the previous object
	 */
	public Object set(E o, int index) {
		if (index >= MAX_SIZE) {
			throw new IllegalArgumentException("index exceeded maximium");
		}
		
		if (o != null && !isDefault(o)) {
			if (index >= array.length) {
				Object newArray[] = new Object[Math.max(index+1, array.length*2)];
				System.arraycopy(array, 0, newArray, 0, array.length);
				array = newArray;
			}
			
			length = Math.max(length, index+1);
			Object old = array[index];
			array[index] = o;
			return old;
		}
		
		if (index >= array.length) {
			return null;
		}
		
		Object old = array[index];
		array[index] = o;
		if (!isDefault(old) && (index == length-1)) {
			while (length > 0) {
				if (!isDefault(array[length-1])) {
					break;
				}
				
				length--;
			}
		}
		
		return old;
	}
	
	/**
	 * Removes all the elements from the list.
	 */
	public void clear() {
		for (int i=0; i<length; i++) {
			array[i] = null;
		}
		length = 0;
	}
	
	
	/**
	 * Gets the object at the specified index.
	 * @param index offset into array
	 * @return the object at index
	 */
	@SuppressWarnings("unchecked")
	public E get(int index) {
		if (index < 0 || index >= array.length) {
			return null;
		}
		
		return (E) array[index];
	}
	
	
	/**
	 * The index of the last non null element.
	 * @return index of the last non null element or 0 if it has no nun null elements
	 */
	public int length() {
		return length;		
	}
	
	
	/**
	 * Copies the content of this collection into a list and returns it.
	 * @return a copy of this collection
	 */
	public Object[] toArray() {
		Object copy[] = new Object[Math.max(0, length)];
		System.arraycopy(array, 0, copy, 0, length);
		return copy;		
	}
	
	
	/**
	 * Trims the capacity of this vector to be the vector's current size.
	 */
	public void trimToSize() {
		if (array.length > length) {
			Object newArray[] = new Object[length];
			System.arraycopy(array, 0, newArray, 0, length);
			array = newArray;
		}
	}
	
	
	/**
	 * Checks if o is a default object.
	 * @param o the object to check
	 * @return true if o is default
	 */
	protected boolean isDefault(Object o) {
		return (o == null);
	}
	
	
	/**
	 * Gets wich elements are used.
	 * @return a string description of this object 
	 */
	public String toString() {
		StringBuffer str = new StringBuffer(array.length);
		for (int i=0; i<array.length; i++) {
			str.append(array[i] != null ? "1" : (i >= length ? "?" : "0"));
		}
		return str.toString();
	}
	
	
	/**
	 * Tests the class.
	 * @param args
	 */
	public static void main(String args[]) {
		ObjectArray<Object> array = new ObjectArray<Object>();
		array.set(new Object(), 3);
		System.out.println(array);		
		array.set(new Object(), 1);
		System.out.println(array);		
		array.set(null, 3);
		System.out.println(array);		
		array.set(null, 1);
		System.out.println(array);		
		System.out.println("length="+array.toArray().length+" "+array.length());		
	}
}

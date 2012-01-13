/*
 * Copyright (c) 2008-2012 Java Scene Graph
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
 * * Neither the name of 'Java Scene Graph' nor the names of its contributors
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


/**
 * An auto resizeing array.
 */
public class IntArray {
	
	// the maximum size of the array.
	private static final int MAX_SIZE = 1024*4;
	
	// the array wich is wrapped
	private int array[] = new int[0];
	
	// the length of the array counting to the last non null element
	private int length = -1;
	
	// the default value used in elements that is not set.
	private int defaultValue;
	
	/**
	 * Creates an IntArray with the default value of 0.
	 */
	public IntArray() {
		this(0);
	}
	
	/**
	 * Creates a auto resiable array with the specified default value.
	 * @param deafultValue the default value.
	 */
	public IntArray(int defaultValue) {
		this.defaultValue = defaultValue;		
	}
	
	
	/**
	 * Sets the int at the specified index.
	 * @param i the int to set
	 * @param index the index into the array
	 * @return the previous int
	 */
	public int set(int b, int index) {
		if (index >= MAX_SIZE) {
			throw new IllegalArgumentException("index exceeded maximium");
		}
		
		if (b != defaultValue) {
			if (index >= array.length) {
				int newArray[] = new int[Math.max(index+1, array.length*2)];
				System.arraycopy(array, 0, newArray, 0, array.length);
				array = newArray;
			}
			
			length = Math.max(length, index+1);
			int old = array[index];
			array[index] = b;
			return old;
		}
		
		// o is null
		if (index >= array.length) {
			return defaultValue;
		}
		
		int old = array[index];
		array[index] = b;
		if (old != defaultValue && index == length-1) {
			while (length > 0) {
				if (array[length-1] != defaultValue) {
					break;
				}
				
				length--;
			}
		}
		
		return old;
	}
	
	
	/**
	 * Gets the element at the specified index.
	 * @param index offset into array
	 * @return the element at index
	 */
	public int get(int index) {
		if (index < 0 || index >= array.length) {
			return defaultValue;
		}
		
		return array[index];
	}
	
	
	/**
	 * The index of the last non null element.
	 * @return index of the last non null element or -1 if it has no nun null elements
	 */
	public int length() {
		return length;		
	}
	
	
	/**
	 * Copies the content of this collection into a list and returns it.
	 * @return a copy of this collection
	 */
	public int[] toArray() {
		int copy[] = new int[Math.max(0, length)];
		System.arraycopy(array, 0, copy, 0, length);
		return copy;		
	}
	
	
	/**
	 * Trims the capacity of this vector to be the vector's current size.
	 */
	public void trimToSize() {
		if (array.length > length) {
			int newArray[] = new int[length];
			System.arraycopy(array, 0, newArray, 0, length);
			array = newArray;
		}
	}
	
	
	/**
	 * Gets wich elements are used.
	 * @return a string description of this object 
	 */
	public String toString() {
		StringBuffer str = new StringBuffer(array.length);
		for (int i=0; i<array.length; i++) {
			str.append(array[i] != defaultValue ? "1" : (i >= length ? "?" : "0"));
		}
		return str.toString();
	}
}

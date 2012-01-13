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

import java.io.Serializable;

public class Hash implements Serializable {

	private static final long serialVersionUID = 0L;
	
	public int hash = -1;
	
	public void setSeed(int newSeed) {
		hash = newSeed;
	}
	
	public void addBoolean(boolean value) {
		addInt(value ? 1 : 0);
	}
	
	public void addFloat(float value) {
		addInt(Float.floatToIntBits(value));
	}
	
	public void addInt(int value) {
		hash = value + (hash << 6) + (hash << 16) - hash;
	}
}
///*
// **************************************************************************
// *                                                                        *
// *          General Purpose Hash Function Algorithms Library              *
// *                                                                        *
// * Author: Arash Partow - 2002                                            *
// * URL: http://www.partow.net                                             *
// * URL: http://www.partow.net/programming/hashfunctions/index.html        *
// *                                                                        *
// * Copyright notice:                                                      *
// * Free use of the General Purpose Hash Function Algorithms Library is    *
// * permitted under the guidelines and in accordance with the most current *
// * version of the Common Public License.                                  *
// * http://www.opensource.org/licenses/cpl.php                             *
// *                                                                        *
// **************************************************************************
//*/
//
//
//class GeneralHashFunctionLibrary
//{
//
//
//   public long RSHash(String str)
//   {
//      int b     = 378551;
//      int a     = 63689;
//      long hash = 0;
//
//      for(int i = 0; i < str.length(); i++)
//      {
//         hash = hash * a + str.charAt(i);
//         a    = a * b;
//      }
//
//      return hash;
//   }
//   /* End Of RS Hash Function */
//
//
//   public long JSHash(String str)
//   {
//      long hash = 1315423911;
//
//      for(int i = 0; i < str.length(); i++)
//      {
//         hash ^= ((hash << 5) + str.charAt(i) + (hash >> 2));
//      }
//
//      return hash;
//   }
//   /* End Of JS Hash Function */
//
//
//   public long PJWHash(String str)
//   {
//      long BitsInUnsignedInt = (long)(4 * 8);
//      long ThreeQuarters     = (long)((BitsInUnsignedInt  * 3) / 4);
//      long OneEighth         = (long)(BitsInUnsignedInt / 8);
//      long HighBits          = (long)(0xFFFFFFFF) << (BitsInUnsignedInt - OneEighth);
//      long hash              = 0;
//      long test              = 0;
//
//      for(int i = 0; i < str.length(); i++)
//      {
//         hash = (hash << OneEighth) + str.charAt(i);
//
//         if((test = hash & HighBits)  != 0)
//         {
//            hash = (( hash ^ (test >> ThreeQuarters)) & (~HighBits));
//         }
//      }
//
//      return hash;
//   }
//   /* End Of  P. J. Weinberger Hash Function */
//
//
//   public long ELFHash(String str)
//   {
//      long hash = 0;
//      long x    = 0;
//
//      for(int i = 0; i < str.length(); i++)
//      {
//         hash = (hash << 4) + str.charAt(i);
//
//         if((x = hash & 0xF0000000L) != 0)
//         {
//            hash ^= (x >> 24);
//         }
//         hash &= ~x;
//      }
//
//      return hash;
//   }
//   /* End Of ELF Hash Function */
//
//
//   public long BKDRHash(String str)
//   {
//      long seed = 131; // 31 131 1313 13131 131313 etc..
//      long hash = 0;
//
//      for(int i = 0; i < str.length(); i++)
//      {
//         hash = (hash * seed) + str.charAt(i);
//      }
//
//      return hash;
//   }
//   /* End Of BKDR Hash Function */
//
//
//   public long SDBMHash(String str)
//   {
//      long hash = 0;
//
//      for(int i = 0; i < str.length(); i++)
//      {
//         hash = str.charAt(i) + (hash << 6) + (hash << 16) - hash;
//      }
//
//      return hash;
//   }
//   /* End Of SDBM Hash Function */
//
//
//   public long DJBHash(String str)
//   {
//      long hash = 5381;
//
//      for(int i = 0; i < str.length(); i++)
//      {
//         hash = ((hash << 5) + hash) + str.charAt(i);
//      }
//
//      return hash;
//   }
//   /* End Of DJB Hash Function */
//
//
//   public long DEKHash(String str)
//   {
//      long hash = str.length();
//
//      for(int i = 0; i < str.length(); i++)
//      {
//         hash = ((hash << 5) ^ (hash >> 27)) ^ str.charAt(i);
//      }
//
//      return hash;
//   }
//   /* End Of DEK Hash Function */
//
//
//   public long BPHash(String str)
//   {
//      long hash = 0;
//
//      for(int i = 0; i < str.length(); i++)
//      {
//         hash = hash << 7 ^ str.charAt(i);
//      }
//
//      return hash;
//   }
//   /* End Of BP Hash Function */
//
//
//   public long FNVHash(String str)
//   {
//      long fnv_prime = 0x811C9DC5;
//      long hash = 0;
//
//      for(int i = 0; i < str.length(); i++)
//      {
//      hash *= fnv_prime;
//      hash ^= str.charAt(i);
//      }
//
//      return hash;
//   }
//   /* End Of FNV Hash Function */
//
//
//   public long APHash(String str)
//   {
//      long hash = 0xAAAAAAAA;
//
//      for(int i = 0; i < str.length(); i++)
//      {
//         if ((i & 1) == 0)
//         {
//            hash ^= ((hash << 7) ^ str.charAt(i) ^ (hash >> 3));
//         }
//         else
//         {
//            hash ^= (~((hash << 11) ^ str.charAt(i) ^ (hash >> 5)));
//         }
//      }
//
//      return hash;
//   }
//   /* End Of AP Hash Function */
//
//}

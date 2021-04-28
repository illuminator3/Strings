/*
   Copyright 2021 illuminator3

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package me.illuminator3.strings;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

public class Strings
{
    private static final Unsafe UNSAFE;
    private static final Class<String> CLASS = String.class;
    private static final Field VALUE, CODER;
    private static final boolean VARIANT;

    static {
        try
        {
            Field f = Unsafe.class.getDeclaredField("theUnsafe");

            f.setAccessible(true);

            UNSAFE = (Unsafe) f.get(null);
        } catch (NoSuchFieldException | IllegalAccessException ex)
        {
            throw new RuntimeException(ex);
        }

        // did you know that I hate Java?
        Field value = null, coder;
        boolean variant;

        try
        {
            value = CLASS.getDeclaredField("value");

            if (value.getType().equals(byte[].class))
                throw new IllegalStateException();

            coder = null;
            variant = false;
        } catch (NoSuchFieldException | IllegalStateException ignored)
        {
            try
            {
                if (value == null)
                    throw new IllegalStateException();

                coder = CLASS.getDeclaredField("coder");
            } catch (NoSuchFieldException | IllegalStateException what)
            {
                throw new IllegalStateException("what is this? java 0?");
            }

            variant = true;
        }

        VALUE = value;
        CODER = coder;
        VARIANT = variant;
    }

    public static void modify(String orig, String newval)
    {
        if (!VARIANT) // old way
            UNSAFE.putObject(orig, UNSAFE.objectFieldOffset(VALUE), newval.toCharArray());
        else
        {
            long valueOffset = UNSAFE.objectFieldOffset(VALUE), coderOffset = UNSAFE.objectFieldOffset(CODER);

            UNSAFE.putObject(orig, valueOffset, UNSAFE.getObject(newval, valueOffset));
            UNSAFE.putByte(orig, coderOffset, UNSAFE.getByte(newval, coderOffset));
        }
    }
}
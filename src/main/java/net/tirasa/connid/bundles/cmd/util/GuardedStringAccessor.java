/**
 * Copyright (C) 2020 BCV solutions s.r.o., Czech Republic (http://www.bcvsolutions.eu)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.tirasa.connid.bundles.cmd.util;

import java.util.Arrays;

import org.identityconnectors.common.security.GuardedString;

/**
 * Třída sloužící k získání hesla z objektu třídy GuardedString.
 *
 * @author Jaromír Mlejnek
 */
public class GuardedStringAccessor implements GuardedString.Accessor {

    private char[] array;

    /**
     * Metoda uloži heslo z objektu třídy GuardedString do pole typu char.
     */
    public void access(char[] clearChars) {
        array = new char [clearChars.length];
        System.arraycopy(clearChars, 0, array, 0, clearChars.length);
    }

    /**
     * Metoda navracející heslo v poli typu char.
     *
     * @return Pole typu char, ve kterém je uloženo heslo.
     */
    public char[] getArray() {
        return array;
    }

    /**
     * Metoda vyčistí pole, ve kterém je heslo.
     */
    public void clearArray() {
        Arrays.fill(array, 0, array.length, ' ');
    }

}

package fr.bck.tetralibs.gate;


import net.minecraft.server.level.ServerPlayer;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;


/*≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡
 ≡           Copyright BCK, Inc 2025. (DragClover / Blackknight)                 ≡
 ≡                                                                               ≡
 ≡ Permission is hereby granted, free of charge, to any person obtaining a copy  ≡
 ≡ of this software and associated documentation files (the “Software”), to deal ≡
 ≡ in the Software without restriction, including without limitation the rights  ≡
 ≡ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ≡
 ≡ copies of the Software, and to permit persons to whom the Software is         ≡
 ≡ furnished to do so, subject to the following conditions:                      ≡
 ≡                                                                               ≡
 ≡ The above copyright notice and this permission notice shall be included in    ≡
 ≡ all copies or substantial portions of the Software.                           ≡
 ≡                                                                               ≡
 ≡ THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ≡
 ≡ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ≡
 ≡ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ≡
 ≡ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ≡
 ≡ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ≡
 ≡ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE ≡
 ≡ SOFTWARE.                                                                     ≡
 ≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡*/

public final class BCKGateManager {

    /**
     * clé publique Ed25519 (32 octets)
     */
    private static final byte[] PUB = {(byte) 0x1A, (byte) 0x8C, (byte) 0x4C, (byte) 0xA8, (byte) 0x4C, (byte) 0x77, (byte) 0xDA, (byte) 0x3F, (byte) 0xE5, (byte) 0x80, (byte) 0xAC, (byte) 0x8A, (byte) 0xB7, (byte) 0xDC, (byte) 0xEC, (byte) 0x60, (byte) 0x52, (byte) 0x1F, (byte) 0xF3, (byte) 0xE9, (byte) 0xF1, (byte) 0x04, (byte) 0x94, (byte) 0xC5, (byte) 0x62, (byte) 0x35, (byte) 0xF3, (byte) 0x3A, (byte) 0x22, (byte) 0x24, (byte) 0x94, (byte) 0x84};

    /**
     * Vérifie la Gate Key reçue.
     */
    public static boolean isValid(ServerPlayer player, String token) {
        try {
            /* 1. découpe "payload.signature" (Base64URL) */
            String[] parts = token.split("\\.");
            if (parts.length != 2) return false;

            byte[] payload = Base64.getUrlDecoder().decode(parts[0]);
            byte[] sig = Base64.getUrlDecoder().decode(parts[1]);

            /* 2. vérifie la signature */
            Signature ver = Signature.getInstance("Ed25519");
            ver.initVerify(KeyFactory.getInstance("Ed25519").generatePublic(new X509EncodedKeySpec(PUB)));
            ver.update(payload);
            if (!ver.verify(sig)) return false;          // signature KO

            /* 3. contenu du payload */
            String text = new String(payload, StandardCharsets.UTF_8);

            // Token universel ------ ("GLOBAL")
            if ("GLOBAL".equals(text)) return true;

            // Token personnel ------ (UUID exact du joueur)
            return text.equals(player.getUUID().toString());

        } catch (Exception ignored) {
            return false;
        }
    }

    private BCKGateManager() {
    }
}

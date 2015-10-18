package io.microdev.note.util;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

public class PublicKeyUtil {
    
    private static final String PUBLIC_KEY_ENCODED_A = "44444644466766643734454444444534444446444544674466747736244633634577745546566775555456346675744677366575673334453366377432644552756557636377633437564455345636445474354576526745637337364732456765367433346647557434774564646646664445444665446232437364447453465666575455436746645647566664664434644335564464356773656746426367467334475635774623667676357543567647666536347643644775644534565355444544";
    private static final String PUBLIC_KEY_ENCODED_B = "D9929A1E27B18B979702115611F31181D99237B31151844972A8757FB79943C0D6512492FA11E26160042248B53659B9842BDA67C6952946384A75010F8EF09F87432754492873637791E2A87B9931A90A3351222FAB915AC214392F968BF1E8D21F4E6955E27A7AAC8777C97C1211A4AFCAF42BA433D4FF4B697241DC0E90EA5D120A2966D9126DDE81B904722E96AE857A389A68563B43C403D58774FFE3341B4741C8175288C3F4C7A2777663F08A97D4522A661F6A3283A3778F15559D4381941112";
    
    public static String getPublicKey() {
        // Builder for encoded key
        StringBuilder encodedKeyBuilder = new StringBuilder();
        
        // Build encoded key
        for (int i = 0; i < PUBLIC_KEY_ENCODED_A.length() + PUBLIC_KEY_ENCODED_B.length(); i++) {
            if (i%2 == 0) {
                encodedKeyBuilder.append(PUBLIC_KEY_ENCODED_A.charAt(i/2));
            } else {
                encodedKeyBuilder.append(PUBLIC_KEY_ENCODED_B.charAt((i - 1)/2));
            }
        }
        
        // Get string from encoded key builder
        String encodedKey = encodedKeyBuilder.toString();
        
        // A buffer for raw data represented by encoded key
        ByteBuffer encodedKeyBuffer = ByteBuffer.allocate(encodedKey.length()/2);
        
        // Fill buffer with raw data
        for (int i = 0; i < encodedKey.length(); i += 2) {
            encodedKeyBuffer.put(Byte.parseByte(encodedKey.substring(i, i + 2), 16));
        }
        encodedKeyBuffer.rewind();
        
        // Decode encoded key to char buffer
        CharBuffer keyBuffer = Charset.forName("UTF-8").decode(encodedKeyBuffer);
        
        return keyBuffer.toString();
    }
    
}

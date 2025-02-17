package com.example.Minor1;

import java.math.BigInteger;
import java.security.SecureRandom;

public class RSA {

    private static BigInteger generatePrime(int bitLength) {
        SecureRandom random = new SecureRandom();
        return BigInteger.probablePrime(bitLength, random);
    }

    private static BigInteger extendedEuclidean(BigInteger a, BigInteger b) {
        BigInteger old_r = a, r = b;
        BigInteger old_s = BigInteger.ONE, s = BigInteger.ZERO;
        BigInteger old_t = BigInteger.ZERO, t = BigInteger.ONE;
        
        while (!r.equals(BigInteger.ZERO)) {
            BigInteger quotient = old_r.divide(r);
            BigInteger temp_r = old_r.subtract(quotient.multiply(r));
            old_r = r;
            r = temp_r;

            BigInteger temp_s = old_s.subtract(quotient.multiply(s));
            old_s = s;
            s = temp_s;

            BigInteger temp_t = old_t.subtract(quotient.multiply(t));
            old_t = t;
            t = temp_t;
        }
        return old_s;
    }

    public static RSAKeyPair generateKeys(int bitLength) {
        BigInteger p = generatePrime(bitLength / 2);
        BigInteger q = generatePrime(bitLength / 2);
        BigInteger n = p.multiply(q); // n = p * q
        BigInteger phi = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE)); // φ(n) = (p-1)*(q-1)
        
        BigInteger e = BigInteger.valueOf(65537); 
        while (phi.gcd(e).compareTo(BigInteger.ONE) != 0) {
            e = e.add(BigInteger.TWO);
        }
        
        BigInteger d = extendedEuclidean(e, phi); // d = e^(-1) mod φ(n)
        if (d.signum() == -1) {
            d = d.add(phi); // ensure d is positive
        }

        return new RSAKeyPair(new BigInteger[] { n, e }, new BigInteger[] { n, d });
    }

    // Encrypt a message using the public key (m^e % n)
    public static BigInteger encrypt(BigInteger message, BigInteger n, BigInteger e) {
        return message.modPow(e, n);
    }
    // Decrypt a message using the private key (c^d % n)
    public static BigInteger decrypt(BigInteger ciphertext, BigInteger n, BigInteger d) {
        return ciphertext.modPow(d, n);
    }

    // Utility method to print key details
    public static void printKeyDetails(BigInteger[] key) {
        System.out.println("modulus: " + key[0]);
        System.out.println("exponent: " + key[1]);
    }

    // RSA key pair (public and private)
    public static class RSAKeyPair {
        public final BigInteger[] publicKey;
        public final BigInteger[] privateKey;

        public RSAKeyPair(BigInteger[] publicKey, BigInteger[] privateKey) {
            this.publicKey = publicKey;
            this.privateKey = privateKey;
        }
    }

    public static void main(String[] args) {
        // Generate RSA keys (2048-bit RSA)
        RSAKeyPair keys = generateKeys(2048);

        // Public key
        System.out.println("Sun RSA public key, 2048 bits");
        printKeyDetails(keys.publicKey);

        // Private key
        System.out.println("SunRsaSign RSA private CRT key, 2048 bits");
        printKeyDetails(keys.privateKey);
        String textMessage = "hi myself supragya this is encryption test\r\nand decryption too";
        BigInteger messageBigInt = new BigInteger(textMessage.getBytes());
        BigInteger encryptedText = encrypt(messageBigInt, keys.publicKey[0], keys.publicKey[1]);
        BigInteger decryptedText = decrypt(encryptedText, keys.privateKey[0], keys.privateKey[1]);
        String decryptedMessage = new String(decryptedText.toByteArray());

        System.out.println("Original text message: " + textMessage);
        System.out.println("Encrypted text message: " + encryptedText);
        System.out.println("Decrypted text message: " + decryptedMessage);
        // BigInteger message = new BigInteger("1234567890"); 
        // BigInteger encrypted = encrypt(message, keys.publicKey[0], keys.publicKey[1]);
        // BigInteger decrypted = decrypt(encrypted, keys.privateKey[0], keys.privateKey[1]);

        // System.out.println("Original message: " + message);
        // System.out.println("Encrypted message: " + encrypted);
        // System.out.println("Decrypted message: " + decrypted);
    }
}

// import java.util.ArrayList;
// import java.util.HashSet;
// import java.util.List;
// import java.util.Random;

// public class RSA {
//     private static HashSet<Integer> prime = new HashSet<>();
//     private static Integer public_key = null;
//     private static Integer private_key = null;
//     private static Integer n = null;
//     private static Random random = new Random();

//     public static void main(String[] args)
//     {
//         primeFiller();
//         setKeys();
//        // String message = "Hello My name is Supragya";
//      
//         // System.out.println("Enter the message:");
//         // message = new Scanner(System.in).nextLine();

//         List<Integer> coded = encoder(message);

//         System.out.println("Initial message:");
//         System.out.println(message);
//         System.out.println(
//             "\n\nThe encoded message (encrypted by public key)\n");
//         System.out.println(
//             String.join("", coded.stream()
//                                 .map(Object::toString)
//                                 .toArray(String[] ::new)));
//         System.out.println(
//             "\n\nThe decoded message (decrypted by public key)\n");
//         System.out.println(decoder(coded));
//     }

//     public static void primeFiller()
//     {
//         boolean[] sieve = new boolean[250];
//         for (int i = 0; i < 250; i++) {
//             sieve[i] = true;
//         }

//         sieve[0] = false;
//         sieve[1] = false;

//         for (int i = 2; i < 250; i++) {
//             for (int j = i * 2; j < 250; j += i) {
//                 sieve[j] = false;
//             }
//         }

//         for (int i = 0; i < sieve.length; i++) {
//             if (sieve[i]) {
//                 prime.add(i);
//             }
//         }
//     }

//     public static int pickRandomPrime()
//     {
//         int k = random.nextInt(prime.size());
//         List<Integer> primeList = new ArrayList<>(prime);
//         int ret = primeList.get(k);
//         prime.remove(ret);
//         return ret;
//     }

//     public static void setKeys()
//     {
//         int prime1 = pickRandomPrime();
//         int prime2 = pickRandomPrime();

//         n = prime1 * prime2;
//         int fi = (prime1 - 1) * (prime2 - 1);

//         int e = 2;
//         while (true) {
//             if (gcd(e, fi) == 1) {
//                 break;
//             }
//             e += 1;
//         }

//         public_key = e;

//         int d = 2;
//         while (true) {
//             if ((d * e) % fi == 1) {
//                 break;
//             }
//             d += 1;
//         }

//         private_key = d;
//     }

//     public static int encrypt(int message)
//     {
//         int e = public_key;
//         int encrypted_text = 1;
//         while (e > 0) {
//             encrypted_text *= message;
//             encrypted_text %= n;
//             e -= 1;
//         }
//         return encrypted_text;
//     }

//     public static int decrypt(int encrypted_text)
//     {
//         int d = private_key;
//         int decrypted = 1;
//         while (d > 0) {
//             decrypted *= encrypted_text;
//             decrypted %= n;
//             d -= 1;
//         }
//         return decrypted;
//     }

//     public static int gcd(int a, int b)
//     {
//         if (b == 0) {
//             return a;
//         }
//         return gcd(b, a % b);
//     }

//     public static List<Integer> encoder(String message)
//     {
//         List<Integer> encoded = new ArrayList<>();
//         for (char letter : message.toCharArray()) {
//             encoded.add(encrypt((int)letter));
//         }
//         return encoded;
//     }

//     public static String decoder(List<Integer> encoded)
//     {
//         StringBuilder s = new StringBuilder();
//         for (int num : encoded) {
//             s.append((char)decrypt(num));
//         }
//         return s.toString();
//     }
// }

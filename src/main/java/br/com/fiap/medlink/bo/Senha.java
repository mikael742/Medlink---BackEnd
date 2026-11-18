package br.com.fiap.medlink.bo;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Hash de senha com PBKDF2 (recurso nativo do Java, sem bibliotecas extras).
 * Cada usuário tem um salt aleatório, então senhas iguais geram hashes diferentes.
 */
public final class Senha {

    private static final int ITERACOES = 65_536;
    private static final int TAMANHO_BITS = 256;
    private static final SecureRandom RANDOM = new SecureRandom();

    private Senha() {
    }

    public static String gerarSalt() {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    public static String hash(String senha, String salt) {
        try {
            PBEKeySpec spec = new PBEKeySpec(senha.toCharArray(), Base64.getDecoder().decode(salt), ITERACOES, TAMANHO_BITS);
            byte[] hash = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao gerar hash da senha", e);
        }
    }

    /** Compara em tempo constante para não dar pistas por tempo de resposta. */
    public static boolean confere(String senha, String salt, String hashEsperado) {
        return MessageDigest.isEqual(hash(senha, salt).getBytes(), hashEsperado.getBytes());
    }

    /** Utilitário para gerar hash de senhas de teste: java Senha 123456 */
    public static void main(String[] args) {
        String salt = gerarSalt();
        System.out.println("SALT=" + salt);
        System.out.println("HASH=" + hash(args[0], salt));
    }
}

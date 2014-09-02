/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.cxf.rs.security.oauth2.jwe;

import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.crypto.SecretKey;

import org.apache.cxf.rs.security.oauth2.jwt.Algorithm;
import org.apache.cxf.rs.security.oauth2.utils.Base64UrlUtility;
import org.apache.cxf.rs.security.oauth2.utils.crypto.CryptoUtils;

public class AesGcmWrapKeyEncryptionAlgorithm extends AbstractWrapKeyEncryptionAlgorithm {
    private static final Set<String> SUPPORTED_ALGORITHMS = new HashSet<String>(
        Arrays.asList(Algorithm.A128GCMKW.getJwtName(),
                      Algorithm.A192GCMKW.getJwtName(),
                      Algorithm.A256GCMKW.getJwtName()));
    public AesGcmWrapKeyEncryptionAlgorithm(String encodedKey, String keyAlgoJwt) {    
        this(CryptoUtils.decodeSequence(encodedKey), keyAlgoJwt);
    }
    public AesGcmWrapKeyEncryptionAlgorithm(byte[] keyBytes, String keyAlgoJwt) {
        this(CryptoUtils.createSecretKeySpec(keyBytes, Algorithm.AES_ALGO_JAVA),
             keyAlgoJwt);
    }
    public AesGcmWrapKeyEncryptionAlgorithm(SecretKey key, String keyAlgoJwt) {
        super(key, keyAlgoJwt, true, SUPPORTED_ALGORITHMS);
    }
    
    @Override
    public byte[] getEncryptedContentEncryptionKey(JweHeaders headers, byte[] cek) {
        byte[] wrappedKeyAndTag = super.getEncryptedContentEncryptionKey(headers, cek);
        byte[] wrappedKey = new byte[wrappedKeyAndTag.length - 128 / 8]; 
        System.arraycopy(wrappedKeyAndTag, 0, wrappedKey, 0, wrappedKeyAndTag.length - 128 / 8);
        String encodedTag = Base64UrlUtility.encodeChunk(wrappedKeyAndTag, 
                                                         wrappedKeyAndTag.length - 128 / 8, 128 / 8);
        headers.setHeader("tag", encodedTag);
        return wrappedKey;
    }
    protected AlgorithmParameterSpec getAlgorithmParameterSpec(JweHeaders headers) {
        byte[] iv = CryptoUtils.generateSecureRandomBytes(96 / 8);
        String encodedIv = Base64UrlUtility.encode(iv);
        headers.setHeader("iv", encodedIv);
        return CryptoUtils.getContentEncryptionCipherSpec(128, iv);
    }
}

package com.labate.mentoringme.security.token;

import com.labate.mentoringme.exception.InvalidTokenException;
import com.labate.mentoringme.model.SecureToken;
import com.labate.mentoringme.model.User;
import com.labate.mentoringme.repository.SecureTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.keygen.BytesKeyGenerator;
import org.springframework.security.crypto.keygen.KeyGenerators;
import org.springframework.stereotype.Service;
import org.thymeleaf.util.StringUtils;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Random;

@Service
public class DefaultSecureTokenService implements SecureTokenService {

  private static final BytesKeyGenerator DEFAULT_TOKEN_GENERATOR = KeyGenerators.secureRandom(15);
  private static final Charset US_ASCII = StandardCharsets.US_ASCII;
  @Autowired SecureTokenRepository secureTokenRepository;

  @Value("${labate.security.token.validity}")
  private int tokenValidityInSeconds;

  @Override
  public SecureToken createSecureToken(User user) {
    var tokenValue = generateToken(); // this is a sample, you can adapt as per your security need
    var secureToken = new SecureToken();
    secureToken.setToken(tokenValue);
    secureToken.setExpireAt(LocalDateTime.now().plusSeconds(getTokenValidityInSeconds()));
    secureToken.setUser(user);
    this.saveSecureToken(secureToken);
    return secureToken;
  }

  private String generateToken() {
    return String.valueOf(generateOTP());
  }

  public int generateOTP() {
    var random = new Random();
    return 100000 + random.nextInt(900000);
  }

  @Override
  public void saveSecureToken(SecureToken token) {
    secureTokenRepository.save(token);
  }

  @Override
  public SecureToken findByToken(String token) {
    return secureTokenRepository.findByToken(token);
  }

  @Override
  public void removeToken(SecureToken token) {
    secureTokenRepository.delete(token);
  }

  @Override
  public void removeTokenByToken(String token) {
    secureTokenRepository.removeByToken(token);
  }

  public int getTokenValidityInSeconds() {
    return tokenValidityInSeconds;
  }

  public SecureToken getValidSecureToken(String token) throws InvalidTokenException {
    var secureToken = findByToken(token);
    if (Objects.isNull(secureToken)
        || !StringUtils.equals(token, secureToken.getToken())
        || secureToken.isExpired()) {
      throw new InvalidTokenException("Token is not valid");
    }
    return secureToken;
  }

  @Override
  public SecureToken getValidSecureToken(String token, String email) throws InvalidTokenException {
    var secureToken = getValidSecureToken(token);
    var user = secureToken.getUser();
    if (Objects.isNull(user) || !user.getEmail().equals(email)) {
      throw new InvalidTokenException("Invalid token");
    }
    return secureToken;
  }
}

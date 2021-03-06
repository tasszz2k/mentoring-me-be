package com.labate.mentoringme.service.verification;

import com.labate.mentoringme.dto.request.VerifyTokenRequest;
import com.labate.mentoringme.exception.InvalidTokenException;
import com.labate.mentoringme.model.User;

public interface AccountVerificationService {
  boolean sendRegistrationConfirmationEmail(final User user);

  boolean verifyUser(final String token) throws InvalidTokenException;

  boolean verifyToken(VerifyTokenRequest request) throws InvalidTokenException;
}

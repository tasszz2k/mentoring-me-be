package com.labate.mentoringme.dto.request;

import lombok.Data;

import javax.validation.constraints.AssertTrue;

@Data
public class ForgotPasswordRequest {
  private String email;
  private String phoneNumber;

  @AssertTrue(message = "email or phone number is required")
  private boolean isEmailOrPhoneNumberExists() {
    return phoneNumber != null || email != null;
  }
}

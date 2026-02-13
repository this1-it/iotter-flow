package it.thisone.iotter.ui.signup;


import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class CredentialInput {

	@NotNull(message = "Username is required")
    @Size(max = 255, message = "username must be at most 255 characters")
    private String username;

	@NotNull(message = "Email is required")
    @Size(max = 255, message = "Email must be at most 255 characters")
    private String email;

    @NotNull(message = "Password is required")
    @Size(min = 8, max = 255, message = "Password must be between 8 and 255 characters")
    private String password;

    @NotNull(message = "Password confirmation is required")
    @Size(min = 8, max = 255, message = "Password confirmation must be between 8 and 255 characters")
    private String passwordConfirmation;

    @Size(max = 255, message = "activationKey must be at most 255 characters")
    private String activationKey;

    @Size(max = 255, message = "activationKey must be at most 255 characters")
    private String serialNumber;


    // Getters and setters

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        if (username != null) {
            this.username = username.trim();
        } else {
            this.username = null;
        }
    }

    public String getEmail() {
        return email;
    }

    // Transform email to lower-case on set
    public void setEmail(String email) {
        if(email != null) {
            this.email = email.trim().toLowerCase();
        } else {
            this.email = null;
        }
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        if(password != null) {
            this.password = password.trim();
        } else {
            this.password = null;
        }
    }

    public String getPasswordConfirmation() {
        return passwordConfirmation;
    }

    public void setPasswordConfirmation(String passwordConfirmation) {
        if(passwordConfirmation != null) {
            this.passwordConfirmation = passwordConfirmation.trim();
        } else {
            this.passwordConfirmation = null;
        }
    }

    public String getActivationKey() {
        return activationKey;
    }

    public void setActivationKey(String activationKey) {
        if(activationKey != null) {
            this.activationKey = activationKey.trim();
        } else {
            this.activationKey = null;
        }
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        if (serialNumber != null) {
            this.serialNumber = serialNumber.trim();
        } else {
            this.serialNumber = null;
        }
    }


}

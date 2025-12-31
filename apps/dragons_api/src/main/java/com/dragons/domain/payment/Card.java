package com.dragons.domain.payment;

public class Card {
  private final String cardNumber;
  private final int expiryMonth;
  private final int expiryYear;
  private final String cvc;

  public static Card of(String cardNumber, int expiryMonth, int expiryYear, String cvc) {
    return new Card(cardNumber, expiryMonth, expiryYear, cvc);
  }

  public static String masking(String cardNumber) {
    int length = cardNumber.length();
    if (length < 10) {
      return cardNumber;
    }
    return cardNumber.substring(0, 6) + "*".repeat(length - 10) + cardNumber.substring(length - 4);
  }

  private Card(String cardNumber, int expiryMonth, int expiryYear, String cvc) {
    this.cardNumber = cardNumber;
    this.expiryMonth = expiryMonth;
    this.expiryYear = expiryYear;
    this.cvc = cvc;
  }

  public String cardNumber() {
    return cardNumber;
  }

  public int expiryMonth() {
    return expiryMonth;
  }

  public int expiryYear() {
    return expiryYear;
  }

  public String cvc() {
    return cvc;
  }

}

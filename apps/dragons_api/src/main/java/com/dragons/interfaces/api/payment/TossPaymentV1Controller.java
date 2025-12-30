package com.dragons.interfaces.api.payment;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments/toss")
public class TossPaymentV1Controller {

  @PostMapping
  public void request() {

  }

  @GetMapping("/success")
  public void success() {

  }

  @GetMapping("/fail")
  public void fail() {

  }

}

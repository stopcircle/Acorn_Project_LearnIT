package com.learnit.learnit.payment.kakao.controller;

import com.learnit.learnit.payment.common.LoginRequiredException;
import com.learnit.learnit.payment.common.PaymentException;
import com.learnit.learnit.payment.common.dto.PaymentRequestDTO;
import com.learnit.learnit.payment.kakao.dto.KakaoReadyResponse;
import com.learnit.learnit.payment.kakao.service.PaymentKakaoService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

//카카오페이 전용 컨트롤러
@Slf4j
@Controller
@RequiredArgsConstructor
public class PaymentKakaoController {

    private final PaymentKakaoService paymentKakaoService;

    //결제 준비 Ready (API)
    @PostMapping("/payments/kakao/ready")
    @ResponseBody
    public KakaoReadyResponse kakaoPayReady(@RequestBody PaymentRequestDTO request, HttpSession session){

        Long userId = (Long) session.getAttribute("LOGIN_USER_ID");

        if(userId == null) throw new LoginRequiredException("로그인이 필요한 서비스입니다.");

        return paymentKakaoService.ready(userId, request.getTotalPrice(), request.getCourseIds(), request.getCouponId());
    }

    //결제 승인 Approve (API)
    @GetMapping("/payments/kakao/success")
    public String kakaoPaySuccess(@RequestParam("pg_token") String pg_token,
                                  @RequestParam("orderNo") String orderNo,
                                  RedirectAttributes redirectAttributes) {
        try {
            paymentKakaoService.approve(pg_token, orderNo);
            return "redirect:/payment/result?orderNo=" + orderNo;

        } catch (PaymentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/payment/fail";
        } catch (Exception e) {
            log.error("카카오 승인 처리 중 예상치 못한 오류: {}", e.getMessage());
            return "redirect:/payment/fail";
        }
    }

    @GetMapping("/payments/kakao/cancel")
    public String kakaoPayCancel(@RequestParam("orderNo") String orderNo) {
        paymentKakaoService.cancel(orderNo);
        return "redirect:/payment/fail";
    }


    //결제 Fail
    @GetMapping("/payments/kakao/fail")
    public String kakaoPayFail(@RequestParam("orderNo") String orderNo) {

        paymentKakaoService.fail(orderNo);
        return "redirect:/payment/fail";
    }

}

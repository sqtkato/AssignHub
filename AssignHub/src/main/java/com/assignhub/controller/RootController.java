package com.assignhub.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * アプリケーションのルートURL（/）へのアクセスを処理するコントローラー。
 * 
 * @version 1.00 2026/04/01
 * @author SQT）加藤
 */
@Controller
public class RootController {

    /**
     * ルートURLにアクセスされた場合、ログイン画面（/login）へ自動転送する。
     * @return リダイレクト先のパス
     */
    @GetMapping("/")
    public String root() {
        return "redirect:/login";
    }
}
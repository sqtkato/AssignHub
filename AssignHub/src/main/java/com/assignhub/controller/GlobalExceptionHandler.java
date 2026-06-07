package com.assignhub.controller;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import lombok.extern.slf4j.Slf4j;

/**
 * アプリケーション全体で発生する例外をグローバルに捕捉するクラス。
 * 各Controllerでキャッチされなかった例外はすべてここに集約される。
 * 
 * @version 1.00 2026/04/01
 * @author SQT）加藤
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

	/**
	 * 【追加】favicon.icoなど、静的リソースやURLが見つからない場合(404)の捕捉。
	 * 重大なシステムエラーではないため、ERRORではなくWARNレベルで記録する。
	 */
	@ExceptionHandler(NoResourceFoundException.class)
	public void handleNoResourceFoundException(NoResourceFoundException e) throws Exception {
		// 画面に不要なスタックトレースを出さないよう、警告文だけを1行出す
		log.warn("静的リソースが見つかりません: /{}", e.getResourcePath());
		throw e;
	}

	/**
	 * システム内で発生したその他の全ての例外（500エラー）を捕捉し、ログ出力を行う。
	 */
	@ExceptionHandler(Exception.class)
	public void handleAllExceptions(Exception e) throws Exception {
		
		// コンソールおよびログファイルにスタックトレースを確実に出力
		log.error("【システムエラー】予期せぬ例外が発生しました。", e);

		// ログを出力した後、元の例外をそのまま再スローする
		throw e;
	}
}
package com.assignhub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * AssignHubアプリケーションの起動クラス。
 */
@SpringBootApplication
public class AssignHubApplication {

	/**
	 * アプリケーションのエントリポイント。
	 * @param args コマンドライン引数
	 */
	public static void main(String[] args) {
		SpringApplication.run(AssignHubApplication.class, args);
	}
}
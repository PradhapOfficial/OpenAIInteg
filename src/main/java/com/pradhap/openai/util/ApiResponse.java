package com.pradhap.openai.util;

import java.util.logging.Level;

import com.google.gson.JsonObject;
import com.pradhap.openai.api.OpenAIAPI;

public class ApiResponse {
	public static final int SUCCESS_CODE = 200;
	public static final int INTERNAL_ERROR_CODE = 500;

	int code;
	boolean status;
	String message;

	public ApiResponse(int code, boolean status, String msg) {
		this.code = code;
		this.status = status;
		this.message = msg;
	}

	public int getCode() {
		return code;
	}

	public boolean getStatus() {
		return status;
	}

	public String getMessage() {
		return message;
	}

	public JsonObject setApiResponse(JsonObject responseJsonObj) {
		responseJsonObj.addProperty(OpenAIIntegrationConstants.RESPONSE_CODE, code);
		responseJsonObj.addProperty(OpenAIIntegrationConstants.RESPONSE_STATUS,
				(status) ? OpenAIIntegrationConstants.SUCCESS : OpenAIIntegrationConstants.FAILED);
		responseJsonObj.addProperty(OpenAIIntegrationConstants.RESPONSE_MESSAGE, message);
		return responseJsonObj;
	}
}

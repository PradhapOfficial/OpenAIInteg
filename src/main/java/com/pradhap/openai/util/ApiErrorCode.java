package com.pradhap.openai.util;

import com.google.gson.JsonObject;

public enum ApiErrorCode {
	INTERNAL_ERROR(500, false, "Internal server error"), MESSAGE_KEY_REQUIRED(500, false, "Message key is required");

	int code;
	boolean status;
	String message;

	ApiErrorCode(int code, boolean status, String msg) {
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

	public JsonObject getAsJsonObject() {
		JsonObject responseJsonObj = new JsonObject();
		responseJsonObj.addProperty(OpenAIIntegrationConstants.RESPONSE_CODE, code);
		responseJsonObj.addProperty(OpenAIIntegrationConstants.RESPONSE_STATUS,
				(status) ? OpenAIIntegrationConstants.SUCCESS : OpenAIIntegrationConstants.FAILED);
		responseJsonObj.addProperty(OpenAIIntegrationConstants.RESPONSE_MESSAGE, message);
		return responseJsonObj;
	}
}

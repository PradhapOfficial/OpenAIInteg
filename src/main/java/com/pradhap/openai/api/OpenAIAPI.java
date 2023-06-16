package com.pradhap.openai.api;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.openai.framework.util.ChatResponsePOJO;
import com.openai.framework.util.EditTextResponsePOJO;
import com.openai.framework.util.OpenAI;
import com.openai.framework.util.OpenAIConstants;
import com.pradhap.connection.HttpConnectionConstants;
import com.pradhap.connection.HttpRequest;
import com.pradhap.openai.util.ApiErrorCode;
import com.pradhap.openai.util.ApiResponse;
import com.pradhap.openai.util.OpenAIIntegrationConstants;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class OpenAIAPI extends HttpServlet {
	public static final Logger LOGGER = Logger.getLogger(OpenAIAPI.class.getName());
	static {
		try {
			FileHandler fh = new FileHandler(OpenAIIntegrationConstants.LOG_FILE_PATH, true);
			fh.setFormatter(new SimpleFormatter());
			LOGGER.addHandler(fh);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static final long serialVersionUID = 1L;

	public OpenAIAPI() {
		super();
	}

	public static final String CONTENT = "content";
	public static final String TEXT = "text";

	protected void doPost(HttpServletRequest request, HttpServletResponse response) {
		BufferedWriter bufferedWriter = null;
		JsonObject responseJsonObj = null;
		try {
			String path = request.getServletPath();
			switch (path) {
			case "/api/v1/openai/chat":
				responseJsonObj = createChat(request);
				break;
			case "/api/v1/openai/rephrase":
				responseJsonObj = editText(request);
				break;
			default:
				(responseJsonObj = new JsonObject()).addProperty(OpenAIIntegrationConstants.RESPONSE_STATUS, "failed");
			}
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Exception occured in doPost method :: " + e);
			responseJsonObj = ApiErrorCode.INTERNAL_ERROR.getAsJsonObject();
		} finally {
			try {
				setCode(response, responseJsonObj);
				bufferedWriter = new BufferedWriter(new OutputStreamWriter(response.getOutputStream()));
				response.setContentType("text/html");
				bufferedWriter.write(responseJsonObj.toString());
				bufferedWriter.flush();
				bufferedWriter.close();
			} catch (Exception e) {
				LOGGER.log(Level.SEVERE, "Exception occured in doPost method while close bufferedWriter :: " + e);
			}
		}
	}

	public JsonObject createChat(HttpServletRequest request) {
		BufferedReader bufferedReader = null;
		try {
			LOGGER.log(Level.INFO, "createChat method is called");
			String API_KEY = "Bearer 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";

			JsonObject inputJsonObj = getPayloadAsJsonObject(request.getInputStream());

			String message = inputJsonObj.get(OpenAIIntegrationConstants.MESSAGE).getAsString();

			ChatResponsePOJO chatResponsePojo = OpenAI.createChat(API_KEY, message);
			JsonObject responseJsonObj = new JsonObject();

			if (chatResponsePojo.getError() != null) {
				String errorMessage = chatResponsePojo.getError().get(OpenAIIntegrationConstants.MESSAGE).getAsString();
				responseJsonObj = new ApiResponse(500, false, errorMessage).setApiResponse(responseJsonObj);
				return responseJsonObj;
			}

			JsonArray chatChoicesArray = chatResponsePojo.getChoices();
			JsonObject firstChoice = (JsonObject) chatChoicesArray.get(0).getAsJsonObject();
			JsonObject messageJsonObj = firstChoice.get(OpenAIIntegrationConstants.MESSAGE).getAsJsonObject();

			String content = messageJsonObj.get(CONTENT).getAsString();
			responseJsonObj = new ApiResponse(ApiResponse.SUCCESS_CODE, true, content).setApiResponse(responseJsonObj);
			return responseJsonObj;
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Exception occured in createChat method :: " + e);
			return ApiErrorCode.INTERNAL_ERROR.getAsJsonObject();
		} finally {
			if (bufferedReader != null) {
				try {
					bufferedReader.close();
				} catch (Exception e) {
					LOGGER.log(Level.SEVERE,
							"Exception occured in createChat method while close bufferedReader stream :: " + e);
					return ApiErrorCode.INTERNAL_ERROR.getAsJsonObject();
				}
			}
		}
	}

	public JsonObject editText(HttpServletRequest request) {
		try {
			LOGGER.log(Level.INFO, "editText method is called");
			String API_KEY = "Bearer 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";

			JsonObject inputJsonObj = getPayloadAsJsonObject(request.getInputStream());
			if (!inputJsonObj.has(OpenAIIntegrationConstants.MESSAGE)) {
				throw new Exception("message key is required");
			}

			String message = inputJsonObj.get(OpenAIIntegrationConstants.MESSAGE).getAsString();

			EditTextResponsePOJO editTextResPojo = OpenAI.editText(API_KEY, message);
			JsonObject responseJsonObj = new JsonObject();

			if (editTextResPojo.getError() != null) {
				String errorMessage = editTextResPojo.getError().get(OpenAIIntegrationConstants.MESSAGE).getAsString();
				responseJsonObj = new ApiResponse(500, false, errorMessage).setApiResponse(responseJsonObj);
				return responseJsonObj;
			}

			String phrasedText = (editTextResPojo.getChoices().get(0)).getAsJsonObject().get(TEXT).getAsString();
			responseJsonObj = new ApiResponse(ApiResponse.SUCCESS_CODE, true, phrasedText)
					.setApiResponse(responseJsonObj);
			return responseJsonObj;
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Exception occured in editText method :: " + e);
			return ApiErrorCode.INTERNAL_ERROR.getAsJsonObject();
		}
	}

	public JsonObject getPayloadAsJsonObject(InputStream inputStream) {
		BufferedReader bufferedReader = null;
		try {
			String content;
			StringBuilder inputContent = new StringBuilder();
			bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
			while ((content = bufferedReader.readLine()) != null) {
				inputContent.append(content + "\n");
			}

			if (inputContent.length() == 0) {
				LOGGER.log(Level.WARNING, "message key is required :: ");
				return ApiErrorCode.MESSAGE_KEY_REQUIRED.getAsJsonObject();
			}

			JsonObject inputJsonObj = new Gson().fromJson(inputContent.toString(), JsonObject.class);
			return inputJsonObj;
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Exception occured in getPayloadAsJsonObject method :: " + e);
			return ApiErrorCode.INTERNAL_ERROR.getAsJsonObject();
		} finally {
			if (bufferedReader != null) {
				try {
					bufferedReader.close();
				} catch (Exception e) {
					LOGGER.log(Level.SEVERE,
							"Exception occured in getPayloadAsJsonObject method while close BufferedReader Stream :: "
									+ e);
					return ApiErrorCode.INTERNAL_ERROR.getAsJsonObject();
				}
			}
		}
	}

	public HttpServletResponse setCode(HttpServletResponse response, JsonObject jsonObj) {
		try {
			int code = jsonObj.get(OpenAIIntegrationConstants.RESPONSE_CODE).getAsInt();
			switch (code) {
			case ApiResponse.SUCCESS_CODE:
				response.setStatus(ApiResponse.SUCCESS_CODE);
				break;

			default:
				response.setStatus(ApiResponse.INTERNAL_ERROR_CODE);
			}
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Exception occured while set code :: " + e);
		}
		return response;
	}
}

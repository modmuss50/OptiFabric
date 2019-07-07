package me.modmuss50.optifabric.mod;

public class OptifabricError {

	private static String error = null;
	private static String errorURL = "https://github.com/modmuss50/OptiFabric/blob/master/README.md";
	private static String helpButtonText = "Open Help";

	public static boolean hasError(){
		return getError() != null;
	}

	public static String getError() {
		return error;
	}

	public static String getErrorURL() {
		return errorURL;
	}

	public static void setError(String error){
		OptifabricError.error = error;
	}

	public static void setError(String error, String url){
		setError(error);
		OptifabricError.errorURL = url;
	}

	public static String getHelpButtonText() {
		return helpButtonText;
	}

	public static void setHelpButtonText(String helpButtonText) {
		OptifabricError.helpButtonText = helpButtonText;
	}
}

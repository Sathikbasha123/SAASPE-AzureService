package saaspe.azure.model;
import java.io.IOException;

public enum Type {
	DATETIME, NUMBER, STRING, MICROSOFT_COSTMANAGEMENT_DIMENSIONS;

	public String toValue() {
		switch (this) {
		case MICROSOFT_COSTMANAGEMENT_DIMENSIONS:
			return "microsoft.costmanagement/dimensions";
		case DATETIME:
			break;
		case NUMBER:
			break;
		case STRING:
			break;
		default:
			break;
		}
		return null;
	}

	public static Type forValue(String value) throws IOException {
		if (value.equals("microsoft.costmanagement/dimensions"))
			return MICROSOFT_COSTMANAGEMENT_DIMENSIONS;
		throw new IOException("Cannot deserialize Type");
	}
}

package constant;

public class Constant {

  public static final String HYPHENS = "-";

  public static final String BASE_URL = "https://data.gharchive.org/%s.json.gz";

  public static final String BASE_UNZIPPED_FILE_NAME = "%s.json";

  public static final String OUTPUT_FILE_NAME = "health_scores.csv";

  public static final String ISO_DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";

  public static final String ZONE_UTC = "UTC";

  public static final String EMPTY_STRING = "";

  public static final Double DEFAULT_SCORE = 0.0;

  // if any repo score is set to this value, it will be ignored in calculation
  public static final Double SKIP_SCORE = -1.0;

  // if any value is set to this value, it will be ignored in calculation
  public static final Long SKIP_LONG_VALUE = Long.valueOf(-1);

  public static final int MAX_OUTPUT_ROW = 1000;

  public static final String ANSWER_YES = "Y";

}
